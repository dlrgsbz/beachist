import { requireEnv } from './util'

export interface Config {
    BACKEND_URL: string
    IOT_DATA_ENDPOINT: string
    AWS_REGION: string
    AWS_ACCOUNT_ID: string
    STAGE: string
    IOT_POLICY_NAME: string
    DB_TABLE_NAME: string
    DB_GSI_STATION_DATE: string
    TENANT: string
}

export const config: Config = {
    get BACKEND_URL() {
        throw new Error('BACKEND_URL should not be used anymore')
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
    get DB_TABLE_NAME() {
      return requireEnv('DB_TABLE_NAME')
    },
    get DB_GSI_STATION_DATE() {
      return requireEnv('DB_GSI_STATION_DATE')
    },
    get TENANT() {
      return requireEnv('TENANT')
    }
}
