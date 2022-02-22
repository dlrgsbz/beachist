import { requireEnv } from './utils'

export interface Config {
    BACKEND_URL: string
    IOT_DATA_ENDPOINT: string
    AWS_REGION: string
}

export const config: Config = {
    get BACKEND_URL() {
        return requireEnv('BACKEND_URL')
    },
    get AWS_REGION () {
      return requireEnv('AWS_REGION')
    },
    get IOT_DATA_ENDPOINT() {
      return requireEnv('IOT_DATA_ENDPOINT')
    }
}
