import axios from 'axios'
import { config } from '../../config'
import { ensureEnv } from '../../util'
import { getFields } from '../../lib'
import { iotClient } from '../../aws/iot'
import { logger } from '../../logger'
import { z } from 'zod'

const schema = z.object({
  iotThingName: z.string(),
  stationId: z.string().uuid(),
  appVersionCode: z.number(),
  appVersion: z.string(),
  connected: z.boolean(),
})

type AppInfoUpdatedInput = z.infer<typeof schema>

type AppInfo = Omit<AppInfoUpdatedInput, 'iotThingName' | 'stationId'>

export const appInfoUpdatedHandler = async (event: Partial<AppInfoUpdatedInput>): Promise<void> => {
  try {
    const validatedEvent = validateAppInfoUpdateInput(event)

    const { stationId, iotThingName, ...rest } = validatedEvent

    if (!ensureEnv(iotThingName)) {
      return
    }

    await Promise.all([
      updateStationInfo(stationId, rest),
      publishStationCheck(stationId, iotThingName),
    ])
  } catch (err) {
    if (err instanceof Error) {
      logger.error(err.message)
    } else {
      logger.error(`Error: ${err}`)
    }
  }
}

const updateStationInfo = async (stationId: string, info: Partial<AppInfo>) => axios.post(`${config.BACKEND_URL}station/${stationId}/info`, info)

const publishStationCheck = async (stationId: string, iotThingName: string): Promise <void> => {
  const fields = await getFields(stationId)

  const topic = `fields/${iotThingName}`

  logger.debug(`Got ${fields.length} results, publishing to ${topic}`)

  await iotClient.publish(topic, JSON.stringify(fields), true)
}

const validateAppInfoUpdateInput = (event: Partial<AppInfoUpdatedInput>): AppInfoUpdatedInput => schema.parse(event)
