import { BrnType, publishStationCheck, validateBrn } from '../../lib'
import { EntryRepository, entryRepository } from '../../repository/entryRepository'
import { FieldRepository, fieldRepository } from '../../repository/fieldRepository'
import { IotClient, iotClient } from '../../aws/iot'
import { StationRepository, stationRepository } from '../../repository/stationRepository'

import axios from 'axios'
import { config } from '../../config'
import { ensureEnv } from '../../util'
import { logger } from '../../logger'
import { z } from 'zod'

const refineStationBrn = (string: string) => validateBrn(string, BrnType.Station)

const schema = z.object({
  iotThingName: z.string(),
  stationId: z.string().refine(refineStationBrn),
  appVersionCode: z.number(),
  appVersion: z.string(),
  connected: z.boolean(),
})

type AppInfoUpdatedInput = z.infer<typeof schema>

type AppInfo = Omit<AppInfoUpdatedInput, 'iotThingName' | 'stationId'>

export const appInfoUpdatedHandler = async (event: Partial<AppInfoUpdatedInput>): Promise<void> => {
  await handler(event, stationRepository, fieldRepository, entryRepository, iotClient)
}

export const handler = async (
  event: Partial<AppInfoUpdatedInput>,
  stationRepository: StationRepository,
  fieldRepository: FieldRepository,
  entryRepository: EntryRepository,
  iotClient: IotClient
): Promise<void> => {
  try {
    const validatedEvent = validateAppInfoUpdateInput(event)

    const { stationId, iotThingName, ...rest } = validatedEvent

    if (!ensureEnv(iotThingName)) {
      return
    }

    await Promise.all([
      updateStationInfo(stationId, rest),
      publishStationCheck(stationId, iotThingName, stationRepository, fieldRepository, entryRepository, iotClient),
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

const validateAppInfoUpdateInput = (event: Partial<AppInfoUpdatedInput>): AppInfoUpdatedInput => schema.parse(event)
