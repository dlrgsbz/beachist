import axios from "axios"
import { z } from "zod"

import { config } from '../../config'
import { logger } from '../../logger'
import { IotClient, iotClient } from '../../aws/iot'


enum StateKind {
    broken = 'broken',
    tooLittle = 'tooLittle',
    other = 'other',
}

// todo: should we do validation in the lambda or just don't care and 
// todo: leave validation to the backend where it definitely happens
const schema = z.object({
    iotThingName: z.string(),
    stationId: z.string().uuid(),
    fieldId: z.string().uuid(),
    state: z.boolean(),
    stateKind: z.nativeEnum(StateKind).optional(),
    amount: z.number().int().min(0).optional(),
    note: z.string().optional(),
    crew: z.string(),
})

type CreateEntryInput = z.infer<typeof schema>

export const createEntryHandler = async (event: Partial<CreateEntryInput>): Promise<void> => {
    await handler(event, iotClient)
}

export const handler = async (event: Partial<CreateEntryInput>, iotClient: IotClient): Promise<void> => {
    try {
        const validatedEvent = validateCreateEntryInput(event)

        const { iotThingName, fieldId, stationId, ...rest } = validatedEvent

        console.log(event)

        const result = await axios.post(`${config.BACKEND_URL}station/${stationId}/field/${fieldId}/entry`, rest)

        // todo: publish success to mqtt

        const topic = `entry/${iotThingName}/success`
        console.log({ result: result.data, iotThingName, stationId, fieldId })
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
