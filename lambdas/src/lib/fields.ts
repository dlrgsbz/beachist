import { Entry, EntryRepository } from '../repository/entryRepository'
import { Field, FieldRepository } from '../repository/fieldRepository'

import { Station } from '../repository/stationRepository'
import { formatIso8601Date } from '../util'

type FieldEntry = Field & Partial<Entry>

export const getStationFields = async function ({ fields }: Station, stationId: string, fieldRepository: FieldRepository, entryRepository: EntryRepository): Promise<FieldEntry[]> {
  const stationFields = await Promise.all(
    fields.map(fieldId => fieldRepository.getField(fieldId)),
  )

  const filtered = stationFields.filter((i): i is Field => i !== undefined)

  const entries = await entryRepository.getStationEntries(stationId, formatIso8601Date(new Date()))

  return reduceFields(filtered, entries)
}

// todo: can we somehow do this within DynamoDB?
const reduceFields = (fields: Field[], entries: Entry[]) => {
  return fields.map(field => {
    const entry = entries.find(entry => entry.fieldId === field.id)

    return {
      ...field,
      entry,
    }
  })
}
