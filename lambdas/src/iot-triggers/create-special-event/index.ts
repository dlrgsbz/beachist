import { ApiClient, apiClient } from '../../api/client'
import { IotClient, iotClient } from '../../aws/iot'

import { ensureEnv } from '../../util'
import { logger } from '../../logger'
import { z } from 'zod'

enum SpecialEventType {
  damage = 'damage',
  event = 'event',
}

const schema = z.object({
  iotThingName: z.string(),
  stationId: z.string().uuid(),
  id: z.string().uuid().optional(),
  date: z.string().optional(), // todo: validate date consistent with backend
  note: z.string(),
  title: z.string().min(8),
  kind: z.nativeEnum(SpecialEventType),
  notifier: z.string().min(2),
})

type CreateSpecialEventInput = z.infer<typeof schema>

export const createSpecialEventHandler = async (event: Partial<CreateSpecialEventInput>): Promise<void> => {
  await handler(event, iotClient, apiClient)
}

const handler = async (event: Partial<CreateSpecialEventInput>, iotClient: IotClient, apiClient: ApiClient): Promise<void> => {
  try {
    const validatedEvent = validateCreateSpecialEventPayload(event)

    const { iotThingName, stationId, kind, note, notifier, title, date, id } = validatedEvent

    if (!ensureEnv(iotThingName)) {
      return
    }

    const specialEvent = {
      type: kind,
      note, notifier, title, date, id,
    }

    logger.debug(`Publishing event ${JSON.stringify(specialEvent)}`)

    const createdId = await apiClient.createSpecialEvent(stationId, specialEvent)

    const topic = `special-event/${iotThingName}/success`
    await iotClient.publish(topic, createdId)
  } catch (err) {
    // todo: maybe retry on some errors
    if (err instanceof Error) {
      logger.error(err.message, JSON.stringify(err))
    } else {
      logger.error(`Error: ${err}`)
    }
  }
}

const validateCreateSpecialEventPayload = (event: Partial<CreateSpecialEventInput>): CreateSpecialEventInput => {
  return schema.parse(event)
}
