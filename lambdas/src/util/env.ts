import { config } from "../config"
import { logger } from "../logger"

export const requireEnv = (name: string): string => {
    const value = process.env[name]
    if (value === undefined) {
        throw new Error(`Missing env: ${name}`)
    }
    return value
}

export const ensureEnv = (iotThingName: string): boolean => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [_, env] = iotThingName.split('-')

  if (env !== config.STAGE) {
    logger.info(`Got ${iotThingName} for environment ${config.STAGE}, aborting`)
    return false
  }

  return true
}
