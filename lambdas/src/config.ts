import { requireEnv } from './util'

export interface Config {
    BACKEND_URL: string
    IOT_DATA_ENDPOINT: string
    AWS_REGION: string
    AWS_ACCOUNT_ID: string
    STAGE: string
    IOT_POLICY_NAME: string
}

export const config: Config = {
    get BACKEND_URL() {
        return requireEnv('BACKEND_URL')
    },
    get AWS_REGION () {
      return requireEnv('AWS_REGION')
    },
    get AWS_ACCOUNT_ID () {
      return requireEnv('AWS_ACCOUNT_ID')
    },
    get IOT_DATA_ENDPOINT() {
      return requireEnv('IOT_DATA_ENDPOINT')
    },
    get IOT_POLICY_NAME() {
      return requireEnv('IOT_POLICY_NAME')
    },
    get STAGE() {
      return requireEnv('STAGE')
    },
}
