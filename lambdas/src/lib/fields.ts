import axios from "axios"
import { config } from "../config"
import { formatIso8601Date } from "../util"

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

export const getFields = async (stationId: string): Promise<Field[]> => {
  const date = formatIso8601Date(new Date())

  const [fieldResult, entryResult] = await Promise.all([
    axios.get<Field[]>(`${config.BACKEND_URL}station/${stationId}/field`),
    axios.get<Entry[]>(`${config.BACKEND_URL}entry/${date}/${stationId}`),
  ])

  return reduceFields(fieldResult.data, entryResult.data)
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
