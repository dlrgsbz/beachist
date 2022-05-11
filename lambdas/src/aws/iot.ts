import { CertificateStatus, IoT, ResourceAlreadyExistsException } from '@aws-sdk/client-iot'
import {
  IoTDataPlaneClient,
  PublishCommand,
  UpdateThingShadowCommand,
  UpdateThingShadowCommandOutput,
} from '@aws-sdk/client-iot-data-plane'
import { config } from '../config'
import { logger } from '../logger'
import { Shadow } from '../types'

export interface IotClient {
  updateShadow: (thingName: string, shadow: Shadow) => Promise<UpdateThingShadowCommandOutput>
  publish: (topic: string, data: string) => Promise<void>
  getOrCreateThing: (thingName: string) => Promise<string | undefined>
  setupCertificate: (thingName: string) => Promise<IotCertificateKey | undefined>
  getEndpoints: () => Promise<IotEndpoints | undefined>
}

const awsIotClient: IoT = new IoT({
  region: config.AWS_REGION,
})

const ioTDataPlaneClient: IoTDataPlaneClient = new IoTDataPlaneClient({
  region: config.AWS_REGION,
  endpoint: config.IOT_DATA_ENDPOINT,
})

class IotClientImpl implements IotClient {
  client: IoT
  dataClient: IoTDataPlaneClient

  constructor(client: IoT = awsIotClient, dataClient: IoTDataPlaneClient = ioTDataPlaneClient) {
    this.client = client
    this.dataClient = dataClient
  }

  async publish(topic: string, data: string): Promise<void> {
    await this.dataClient.send(new PublishCommand({ topic, payload: Buffer.from(data, 'utf-8') }))
  }

  async getOrCreateThing(thingName: string): Promise<string | undefined> {
    try {
      const result = await this.client.createThing({
        thingName,
        thingTypeName: 'station',
      })

      if (result.thingArn) {
        return result.thingArn
      }
    } catch (e) {
      if (e instanceof ResourceAlreadyExistsException) {
        const thing = await this.client.describeThing({
          thingName,
        })

        if (thing.thingArn) {
          return thing.thingArn
        }
      }

      logger.error(`error creating thing: ${e}`)
    }

    return undefined
  }

  private async createKeysAndCertificate(): Promise<IotCertificateKey | undefined> {
    const result = await this.client.createKeysAndCertificate({
      setAsActive: true,
    })

    const { certificateArn, certificateId, certificatePem, keyPair } = result

    if (!certificateArn || !certificateId || !certificatePem || !keyPair) {
      logger.error('Couldn\'t get certificate')
      return undefined
    }

    const { PrivateKey, PublicKey } = keyPair
    if (!PrivateKey || !PublicKey) {
      logger.error('Couldn\'t get key pair')
      return undefined
    }

    return {
      certificateArn,
      certificateId,
      certificatePem,
      keyPair: {
        privateKey: PrivateKey,
        publicKey: PublicKey,
      }
    }
  }

  async setupCertificate(thingName: string): Promise<IotCertificateKey | undefined> {
    const certificate = await this.createKeysAndCertificate()
    if (!certificate) {
      return
    }

    const policyDocument = createPolicyDocument(thingName)

    const policyName = `iot-policy-${thingName}`

    try {
      await this.client.createPolicy({
        policyName,
        policyDocument,
      })
    } catch (e) {
      if (e instanceof ResourceAlreadyExistsException) {
        logger.info('IoT Policy already exists, continuing')
      } else {
        logger.error(`Couldn't create IoT policy: ${e}`)
        return undefined
      }
    }

    await this.client.attachPolicy({
      policyName,
      target: certificate.certificateArn,
    })

    await this.client.attachPolicy({
      policyName: config.IOT_POLICY_NAME,
      target: certificate.certificateArn,
    })

    await this.client.attachThingPrincipal({
      principal: certificate.certificateArn,
      thingName,
    })

    const existingCertificates = await this.client.listThingPrincipals({
      thingName,
    })

    if (existingCertificates.principals) {
      await Promise.all(existingCertificates.principals?.map(async (principal) => {
        if (certificate.certificateArn === principal) {
          // we don't want to delete the certificate we just added
          return
        }

        const certificateId = principal.split('/')[1]

        await this.client.updateCertificate({
          certificateId,
          newStatus: CertificateStatus.INACTIVE,
        })

        await this.client.detachThingPrincipal({
          thingName,
          principal,
        })

        await this.client.deleteCertificate({
          certificateId,
          forceDelete: true,
        })
      }))
    }

    return certificate
  }

  async getEndpoints(): Promise<IotEndpoints | undefined> {
    const [dataEndpointResponse, credentialsEndpointResponse] = await Promise.all([
      this.client.describeEndpoint({ endpointType: 'iot:Data-ATS' }),
      this.client.describeEndpoint({ endpointType: 'iot:CredentialProvider' }),
    ])

    const dataEndpoint = dataEndpointResponse.endpointAddress
    const credentialsEndpoint = credentialsEndpointResponse.endpointAddress

    if (!dataEndpoint || !credentialsEndpoint) {
      return undefined
    }

    return { dataEndpoint, credentialsEndpoint }
  }

  async updateShadow(thingName: string, shadow: Shadow): Promise<UpdateThingShadowCommandOutput> {
    const shadowString = JSON.stringify(shadow)
    const command = new UpdateThingShadowCommand({
      thingName,
      payload: Buffer.from(shadowString, 'utf8'),
    })
    const result = await this.dataClient.send(command)
    if (result.$metadata.httpStatusCode === 200) {
      return result;
    }

    throw Error(
      `Update of ${thingName} with ${shadowString} failed. Response from iot client: ${JSON.stringify(
        result,
      )}`,
    );
  }
}

const createPolicyDocument = (thingName: string): string => {
  const policy = {
    Version: '2012-10-17',
    Statement: {
      Effect: "Allow",
      Action: "iot:Connect",
      Resource: `arn:aws:iot:${config.AWS_REGION}:${config.AWS_ACCOUNT_ID}:client/${thingName}`,
    }
  }

  return JSON.stringify(policy)
}


export interface IotCertificateKey {
  certificateArn: string
  certificateId: string
  certificatePem: string
  keyPair: KeyPair
}

export interface KeyPair {
  publicKey: string
  privateKey: string
}

export interface IotEndpoints {
  dataEndpoint: string
  credentialsEndpoint: string
}

export const iotClient: IotClient = new IotClientImpl()
