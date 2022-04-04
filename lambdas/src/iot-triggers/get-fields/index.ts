import axios from "axios"
import { z } from "zod"

import { IotClient, iotClient } from '../../aws/iot'
import { config } from "../../config"
import { logger } from "../../logger"
import { formatIso8601Date } from "../../util"

const schema = z.object({
  iotThingName: z.string(),
  stationId: z.string().uuid(),
})

type GetFieldsInput = z.infer<typeof schema>

/**
 * this type is incomplete on purpose because we only need this here
 */
interface Field {
  id: string // uuid
}

/**
 * this type is incomplete on purpose because we only need this here
 */
interface Entry {
  field: string // uuid
}

export const getFieldsHandler = async (event: Partial<GetFieldsInput>): Promise<void> => {
  await handler(event, iotClient)
}

export const handler = async (event: Partial<GetFieldsInput>, iotClient: IotClient): Promise<void> => {
  try {
    logger.debug(event)

    const validatedEvent = validateGetFieldsInput(event)

    const { iotThingName, stationId } = validatedEvent

    logger.debug(`Field request from ${iotThingName}`)

    const date = formatIso8601Date(new Date())

    const [fieldResult, entryResult] = await Promise.all([
      axios.get<Field[]>(`${config.BACKEND_URL}station/${stationId}/field`),
      axios.get<Entry[]>(`${config.BACKEND_URL}entry/${date}/${stationId}`),
    ])

    const fields = reduceFields(fieldResult.data, entryResult.data)

    const topic = `fields/${iotThingName}`

    logger.debug(`Got ${fields.length} results, publishing to ${topic}`)

    await iotClient.publish(topic, JSON.stringify(fields))
  } catch (err) {
    if (err instanceof Error) {
      logger.error(err.message)
    } else {
      logger.error(`Error: ${err}`)
    }
  }
}

const reduceFields = (fields: Field[], entries: Entry[]) => {
  return fields.map(field => {
    const entry = entries.find(entry => entry.field === field.id)

    return {
      ...field,
      entry,
    }
  })
}

const validateGetFieldsInput = (event: Partial<GetFieldsInput>): GetFieldsInput => {
  return schema.parse(event)
}
