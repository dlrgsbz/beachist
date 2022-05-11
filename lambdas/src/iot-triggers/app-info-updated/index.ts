import axios from 'axios'
import { config } from '../../config'
import { logger } from '../../logger'
import { z } from 'zod'

const schema = z.object({
  stationId: z.string().uuid(),
  appVersionCode: z.number(),
  appVersion: z.string(),
  connected: z.boolean(),
})

type AppInfoUpdatedInput = z.infer<typeof schema>

export const appInfoUpdatedHandler = async (event: Partial<AppInfoUpdatedInput>): Promise<void> => {
  try {
    const validatedEvent = validateAppInfoUpdateInput(event)

    const { stationId, ...rest } = validatedEvent

    await axios.post(`${config.BACKEND_URL}station/${stationId}/info`, rest)
  } catch (err) {
    if (err instanceof Error) {
      logger.error(err.message)
    } else {
      logger.error(`Error: ${err}`)
    }
  }

}

const validateAppInfoUpdateInput = (event: Partial<AppInfoUpdatedInput>): AppInfoUpdatedInput => schema.parse(event)
