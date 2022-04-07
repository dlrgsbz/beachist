import { Duration } from '@aws-cdk/core'

export enum Stage {
    DEV = 'dev',
    STAGING = 'staging',
    PROD = 'prod',
}

export interface EnvironmentProps {
    stage: Stage
    logLevel: 'debug' | 'info' | 'warn' | 'error'
    backendUrl: string
    awsConfig: {
        region: string
        iotDataEndpoint: string
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
    // todo: move this to env
    backendUrl: '***REMOVED***',
    awsConfig: {
        region: 'eu-central-1',
        iotDataEndpoint: 'https://a3dok2ktu19gf9-ats.iot.eu-central-1.amazonaws.com',
    }
}

const STAGING: EnvironmentProps = {
    stage: Stage.STAGING,
    logLevel: 'info',
    // todo: move this to env
    backendUrl: '***REMOVED***',
    awsConfig: {
        region: 'eu-central-1',
        iotDataEndpoint: 'https://a3dok2ktu19gf9-ats.iot.eu-central-1.amazonaws.com',
    }
}

const DEV: EnvironmentProps = {
    stage: Stage.DEV,
    logLevel: 'debug',
    // todo: move this to env
    backendUrl: '***REMOVED***',
    awsConfig: {
        region: 'eu-central-1',
        iotDataEndpoint: 'https://a3dok2ktu19gf9-ats.iot.eu-central-1.amazonaws.com',
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
  