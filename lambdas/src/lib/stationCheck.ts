import { EntryRepository } from '../repository/entryRepository'
import { FieldRepository } from '../repository/fieldRepository'
import { IotClient } from '../aws/iot'
import { StationRepository } from '../repository/stationRepository'
import { getStationFields } from './fields'
import { logger } from '../logger'

export const publishStationCheck = async (
  stationId: string,
  iotThingName: string,
  stationRepository: StationRepository,
  fieldRepository: FieldRepository,
  entryRepository: EntryRepository,
  iotClient: IotClient
): Promise<void> => {
  const station = await stationRepository.getStation(stationId)

  if (!station) {
    logger.warn(`Couldn't find a station for id ${stationId}`)
    return
  }

  const fields = await getStationFields(station, stationId, fieldRepository, entryRepository)

  const topic = `fields/${iotThingName}`

  logger.debug(`Got ${fields.length} results, publishing to ${topic}`)

  await iotClient.publish(topic, JSON.stringify(fields), true)
}
