import { IotClient, iotClient } from '../../aws/iot'

import { ensureEnv } from '../../util'
import { getFields } from '../../lib'
import { logger } from "../../logger"
import { z } from "zod"

const schema = z.object({
  iotThingName: z.string(),
  stationId: z.string().uuid(),
})

type GetFieldsInput = z.infer<typeof schema>

export const getFieldsHandler = async (event: Partial<GetFieldsInput>): Promise<void> => {
  await handler(event, iotClient)
}

export const handler = async (event: Partial<GetFieldsInput>, iotClient: IotClient): Promise<void> => {
  try {
    logger.debug(event)

    const validatedEvent = validateGetFieldsInput(event)

    const { iotThingName, stationId } = validatedEvent

    if (!ensureEnv(iotThingName)) {
      return
    }

    logger.debug(`Field request from ${iotThingName}`)

    const fields = await getFields(stationId)

    const topic = `fields/${iotThingName}`

    logger.debug(`Got ${fields.length} results, publishing to ${topic}`)

    await iotClient.publish(topic, JSON.stringify(fields), true)
  } catch (err) {
    if (err instanceof Error) {
      logger.error(JSON.stringify(err))
    } else {
      logger.error(`Error: ${err}`)
    }
  }
}

const validateGetFieldsInput = (event: Partial<GetFieldsInput>): GetFieldsInput => {
  return schema.parse(event)
}
