import { ApiClient, apiClient } from '../../api/client'

import { logger } from '../../logger'
import { z } from 'zod'

const schema = z.object({
  stationId: z.string().uuid(),
  crew: z.string(),
  date: z.string(), // todo: validate data consistent with backend
})

type UpdateCrewInput = z.infer<typeof schema>

export const updateCrewHandler = async (event: Partial<UpdateCrewInput>): Promise<void> => {
  return handler(event, apiClient)
}

export const handler = async (event: Partial<UpdateCrewInput>, apiClient: ApiClient): Promise<void> => {
  try {
    const validatedEvent = validateUpdateCrewInput(event)

    const { stationId, ...rest } = validatedEvent

    await apiClient.updateCrew(stationId, rest)
  } catch (err) {
    if (err instanceof Error) {
      logger.error(err.message)
    } else {
      logger.error(`Error: ${err}`)
    }
  }
}

const validateUpdateCrewInput = (event: Partial<UpdateCrewInput>): UpdateCrewInput => {
  return schema.parse(event)
}
