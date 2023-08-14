import { BrnType, publishStationCheck, validateBrn } from '../../lib'
import { EntryRepository, entryRepository } from '../../repository/entryRepository'
import { FieldRepository, fieldRepository } from '../../repository/fieldRepository'
import { IotClient, iotClient } from '../../aws/iot'
import { StationRepository, stationRepository } from '../../repository/stationRepository'

import { ensureEnv } from '../../util'
import { logger } from '../../logger'
import { z } from 'zod'

const refineStationBrn = (string: string) => validateBrn(string, BrnType.Station)

const schema = z.object({
  iotThingName: z.string(),
  stationId: z.string().refine(refineStationBrn),
})

type GetFieldsInput = z.infer<typeof schema>

export const getFieldsHandler = async (event: Partial<GetFieldsInput>): Promise<void> => {
  await handler(event, iotClient, stationRepository, fieldRepository, entryRepository)
}

export const handler = async (
  event: Partial<GetFieldsInput>,
  iotClient: IotClient,
  stationRepository: StationRepository,
  fieldRepository: FieldRepository,
  entryRepository: EntryRepository,
): Promise<void> => {
  try {
    logger.debug(event)

    const validatedEvent = validateGetFieldsInput(event)

    const { iotThingName, stationId } = validatedEvent

    if (!ensureEnv(iotThingName)) {
      return
    }

    logger.debug(`Field request from ${iotThingName}`)

    await publishStationCheck(stationId, iotThingName, stationRepository, fieldRepository, entryRepository, iotClient)
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
