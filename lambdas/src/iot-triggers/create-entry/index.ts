import { BrokenEntry, Entry, EntryRepository, StateKind, entryRepository } from '../../repository/entryRepository'
import { Field, FieldRepository, fieldRepository } from '../../repository/fieldRepository'
import { IotClient, iotClient } from '../../aws/iot'

import { ensureEnv } from '../../util'
import { logger } from '../../logger'
import { z } from 'zod'

//  todo: in the frontend we use yup, so maybe only use one library in this project?
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
type CreateEntryInputInput = z.input<typeof schema>

export const createEntryHandler = async (event: Partial<CreateEntryInput>): Promise<void> => {
  await handler(event, iotClient, fieldRepository, entryRepository)
}

export const handler = async (event: Partial<CreateEntryInput>, iotClient: IotClient, fieldRepo: FieldRepository, entryRepository: EntryRepository): Promise<void> => {
  try {
    const validatedEvent = validateCreateEntryInput(event)

    const { iotThingName, ...rest } = validatedEvent

    if (!ensureEnv(iotThingName)) {
      return
    }

    const field = await fieldRepo.getField(rest.fieldId)

    if (!field) {
      return
    }

    const entry = {
      date: new Date(),
      name, required,
      ...rest,
    }

    const result = await entryRepository.createEntry(entry)

    const topic = `entry/${iotThingName}/success`
    await iotClient.publish(topic, result)
  } catch (err) {
    if (err instanceof Error) {
      logger.error(err.message)
    } else {
      logger.error(`Error: ${err}`)
    }
  }
}

const createEntry = (input: Omit<CreateEntryInput, 'iotThingName'>, field: Field): Entry => {
  const { state, stateKind, note, amount, ...rest } = input
  const { name, required } = field
  const common = { date: new Date(), name, required, ...rest }

  if (state) {
    return {
      state: true,
      ...common,
      ...rest,
    }
  } else {
    const bad = { ...common, state: false as const }
    switch (stateKind) {
      case StateKind.broken:
        return {
          ...bad,
          stateKind, note,
        } as BrokenEntry
      case StateKind.other:
        return {
          ...bad,
          stateKind,
          note: note!,
        }
      case StateKind.tooLittle:
        return {
          ...bad,
          stateKind,
          required: required!,
          amount: amount!,
        }
    }
  }
}

const validateCreateEntryInput = (event: Partial<CreateEntryInput>): CreateEntryInput => {
  return schema.parse(event)
}
