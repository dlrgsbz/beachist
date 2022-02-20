import { requireEnv } from './utils'

export interface Config {
    BACKEND_URL: string
}

export const config: Config = {
    get BACKEND_URL() {
        return requireEnv('BACKEND_URL')
    }
}
