import { IotClient, iotClient } from "../../aws/iot";

import axios from "axios";
import { config } from "../../config";
import { ensureEnv } from "../../util";
import { logger } from "../../logger";
import { z } from "zod";

enum EventType {
  firstAid = 'firstAid',
  search = 'search',
}

const schema = z.object({
  iotThingName: z.string(),
  stationId: z.string().uuid(),
  type: z.nativeEnum(EventType),
  id: z.string().uuid(),
  date: z.string(), // todo: validate data consistent with backend
})

type CreateEventInput = z.infer<typeof schema>

interface IdResponse {
  id: string
}

export const createEventHandler = async (event: Partial<CreateEventInput>): Promise<void> => {
  await handler(event, iotClient)
}

export const handler = async (event: Partial<CreateEventInput>, iotClient: IotClient): Promise<void> => {
  try {
    const validatedEvent = validateCreateEventInput(event)

    const { iotThingName, stationId, ...rest } = validatedEvent

    if (!ensureEnv(iotThingName)) {
      return
    }

    const result = await axios.post<IdResponse>(`${config.BACKEND_URL}station/${stationId}/event`, rest)

    const { id } = result.data

    const topic = `events/${iotThingName}`
    logger.debug(`Got result ${id}, publishing to ${topic}`)
    await iotClient.publish(topic, id)
  } catch (err) {
    if (err instanceof Error) {
      logger.error(err.message)
    } else {
      logger.error(`Error: ${err}`)
    }
  }
}

const validateCreateEventInput = (event: Partial<CreateEventInput>): CreateEventInput => {
  return schema.parse(event)
}
