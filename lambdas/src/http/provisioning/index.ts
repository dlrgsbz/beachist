import { APIGatewayProxyEvent, APIGatewayProxyResult } from "aws-lambda"
import { IotClient, iotClient } from "../../aws/iot"

import axios from "axios"
import { config } from "../../config"
import { logger } from "../../logger"

export const provisioningHandler = async (event: APIGatewayProxyEvent): Promise<APIGatewayProxyResult> => {
  const { authorization } = event.headers

  if (!authorization) {
    return {
      statusCode: 401,
      body: JSON.stringify({
        errors: [
          'no credentials provided'
        ]
      })
    }
  }

  const credentials = extractCredentials(authorization)

  if (!credentials) {
    return {
      statusCode: 401,
      body: JSON.stringify({
        errors: [
          'no credentials provided'
        ]
      })
    }
  }

  if (!event.body) {
    return {
      statusCode: 400,
      body: JSON.stringify({
        errors: [
          'missing station id'
        ]
      })
    }
  }

  const body = JSON.parse(event.body)

  // todo: check if body actually has station

  const input = { ...credentials, station: body.station }

  return handler(input, iotClient)
}

interface ProvisioningHandlerInput {
  username: string
  password: string
  station: number
}

const handler = async (event: ProvisioningHandlerInput, iotClient: IotClient): Promise<APIGatewayProxyResult> => {
  const result = await axios.post<ProvisioningResponse>(`${config.BACKEND_URL}provision`, null, {
    auth: {
      username: event.username,
      password: event.password,
    },
  })

  if (result.status === 401) {
    return {
      statusCode: 401,
      body: JSON.stringify({
        errors: [
          'wrong credentials provided'
        ]
      })
    }
  }

  if (result.status < 200 || result.status >= 300) {
    const data = result.data
    logger.warn(`Got result ${data}`)
    return create500()
  }

  const { stationId } = result.data

  const thingName = `station-${config.STAGE}-${event.station}`
  logger.info(`Provisioning ${thingName}`)
  const thingArn = await iotClient.getOrCreateThing(thingName)

  if (!thingArn) {
    return create500()
  }

  const [certificate, endpoints] = await Promise.all([
    iotClient.setupCertificate(thingName),
    iotClient.getEndpoints(),
  ])

  if (!certificate || !endpoints) {
    return create500()
  }

  await iotClient.updateShadow(thingName, {
    state: {
      reported: {
        stationId,
      },
      desired: {
        stationId,
      }
    }
  })

  return {
    statusCode: 200,
    body: JSON.stringify({
      privateKey: certificate.keyPair.privateKey,
      publicKey: certificate.keyPair.publicKey,
      certificatePem: certificate.certificatePem,
      certificateId: certificate.certificateId,
      thingName,
      ...endpoints,
    })
  }
}

const create500 = () => ({
  statusCode: 500,
  body: JSON.stringify({
    errors: [
      'internal server error'
    ]
  })
})

const extractCredentials = (header: string): { username: string; password: string } | undefined => {
  const base64 = header.split(' ')[1]

  if (!base64) {
    return undefined
  }

  const decoded = Buffer.from(base64, 'base64').toString('utf8')

  const [username, password] = decoded.split(':')

  if (!username || !password) {
    return undefined
  }

  return { username, password }
}

interface ProvisioningResponse {
  stationId: string
}
