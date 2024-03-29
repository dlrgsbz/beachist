import { IotClient, iotClient } from '../../aws/iot'

import axios from "axios"
import { config } from '../../config'
import { ensureEnv } from '../../util'
import { logger } from '../../logger'
import { z } from "zod"

enum StateKind {
    broken = 'broken',
    tooLittle = 'tooLittle',
    other = 'other',
}

// todo: should we do validation in the lambda or just don't care and 
//  leave validation to the backend where it definitely happens
//  also: in the frontend we use yup, so maybe only use one library in 
//  this project?
const schema = z.object({
    iotThingName: z.string(),
    stationId: z.string().uuid(),
    fieldId: z.string().uuid(),
    state: z.boolean(),
    stateKind: z.nativeEnum(StateKind).optional(),
    amount: z.number().int().min(0).optional(),
    note: z.string().optional(),
    crew: z.string().optional(),
})

type CreateEntryInput = z.infer<typeof schema>

export const createEntryHandler = async (event: Partial<CreateEntryInput>): Promise<void> => {
    await handler(event, iotClient)
}

export const handler = async (event: Partial<CreateEntryInput>, iotClient: IotClient): Promise<void> => {
    try {
        const validatedEvent = validateCreateEntryInput(event)

        const { iotThingName, fieldId, stationId, ...rest } = validatedEvent

        if (!ensureEnv(iotThingName)) {
          return
        }
    
        const result = await axios.post(`${config.BACKEND_URL}station/${stationId}/field/${fieldId}/entry`, rest)

        const topic = `entry/${iotThingName}/success`
        await iotClient.publish(topic, result.data.id)
    } catch (err) {
        if (err instanceof Error) {
            logger.error(err.message)
        } else {
            logger.error(`Error: ${err}`)
        }
    }
}

const validateCreateEntryInput = (event: Partial<CreateEntryInput>): CreateEntryInput => {
    return schema.parse(event)
}
