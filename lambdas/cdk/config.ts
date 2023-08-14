import { Duration } from "aws-cdk-lib"

export enum Stage {
    DEV = 'dev',
    STAGING = 'staging',
    PROD = 'prod',
}

const requireEnv = (name: string): string => {
  const value = process.env[name]
  if (value === undefined) {
      throw new Error(`Missing env: ${name}`)
  }
  return value
}
export interface EnvironmentProps {
    stage: Stage
    logLevel: 'debug' | 'info' | 'warn' | 'error'
    backendUrl: string
    awsConfig: {
        region: string
        iotDataEndpoint: string
        certificateArn: string
    }
    tenant: string
    domainName: string
}

export const Timeouts = {
    connectionTimeout: Duration.seconds(3),
    requestTimeout: Duration.seconds(3),
    lambdaTimeout: Duration.seconds(6),
}

const PROD: EnvironmentProps = {
    stage: Stage.PROD,
    logLevel: 'info',
    backendUrl: requireEnv('BACKEND_URL'),
    awsConfig: {
        region: 'eu-central-1',
        iotDataEndpoint: requireEnv('IOT_DATA_ENDPOINT'),
        certificateArn: 'todo',
    },
    tenant: 'dlrgsbz',
    domainName: requireEnv('DOMAIN_NAME'),
}

const STAGING: EnvironmentProps = {
    stage: Stage.STAGING,
    logLevel: 'info',
    backendUrl: requireEnv('BACKEND_URL'),
    awsConfig: {
        region: 'eu-central-1',
        iotDataEndpoint: requireEnv('IOT_DATA_ENDPOINT'),
        certificateArn: 'todo',
    },
    tenant: 'beachist',
    domainName: requireEnv('DOMAIN_NAME'),
}

const DEV: EnvironmentProps = {
    stage: Stage.DEV,
    logLevel: 'debug',
    backendUrl: requireEnv('BACKEND_URL'),
    awsConfig: {
        region: 'eu-central-1',
        iotDataEndpoint: requireEnv('IOT_DATA_ENDPOINT'),
        certificateArn: 'arn:aws:acm:eu-central-1:431471835287:certificate/897a2a23-8e09-42aa-a480-779e3418ae9a',
    },
    tenant: 'beachist',
    domainName: requireEnv('DOMAIN_NAME'),
}

export const environmentProps: EnvironmentProps = ((stage?: string): EnvironmentProps => {
    switch (stage) {
      case 'prod':
        return PROD
      case 'staging':
        return STAGING
      default:
        return DEV
    }
  })(process.env.STAGE)
