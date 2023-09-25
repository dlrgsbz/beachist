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
    }
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
    }
}

const STAGING: EnvironmentProps = {
    stage: Stage.STAGING,
    logLevel: 'info',
    backendUrl: requireEnv('BACKEND_URL'),
    awsConfig: {
        region: 'eu-central-1',
    }
}

const DEV: EnvironmentProps = {
    stage: Stage.DEV,
    logLevel: 'debug',
    backendUrl: requireEnv('BACKEND_URL'),
    awsConfig: {
        region: 'eu-central-1',
    }
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
  