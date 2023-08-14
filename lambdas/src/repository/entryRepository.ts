import '../../types/reset'

import { AttributeValue, DynamoDBClient, PutItemCommand, QueryCommand } from '@aws-sdk/client-dynamodb'
import { BrnType, generateBrn } from '../lib'
import { formatIso8601Date, parseIso8601Date } from '../util'

import { config } from '../config'
import { dynamoClient } from './dynamoClient'

interface BaseEntry {
  // fields from Field
  name: string
  required?: number
  // actual Entry fields
  stationId: string
  fieldId: string
  date: Date
  crew?: string
}

interface GoodEntry extends BaseEntry {
  state: true
}

interface TooLittleEntry extends BaseEntry {
  state: false
  stateKind: StateKind.tooLittle
  required: number
  amount: number
}

export interface BrokenEntry extends BaseEntry {
  state: false
  stateKind: StateKind.broken
  note: string
}

export interface OtherEntry extends BaseEntry {
  state: false
  stateKind: StateKind.other
  note: string
}

type BadEntry = TooLittleEntry | BrokenEntry | OtherEntry
export type Entry = GoodEntry | BadEntry

// export interface Entry {
//   // fields from Field
//   name: string
//   required?: number
//   // actual Entry fields
//   stationId: string
//   fieldId: string
//   state: boolean
//   stateKind?: StateKind
//   amount?: number
//   note?: string
//   crew?: string
//   date: Date
// }

export interface EntryRepository {
  createEntry(entry: Entry): Promise<string>
  getStationEntries(stationId: string, date: string): Promise<Entry[]>
}

class EntryRepositoryImpl implements EntryRepository {
  constructor(private readonly dynamoClient: DynamoDBClient) {}

  async createEntry({ crew, fieldId, state, stationId, date, required, name, ...rest }: Entry): Promise<string> {
    const id = generateBrn(BrnType.Entry);

    let stateKind;
    if (!state) {
      stateKind = (rest as BadEntry).stateKind
    }
    let amount;
    if (stateKind === StateKind.tooLittle) {
      amount = (rest as TooLittleEntry).amount
    }
    let note;
    if (stateKind === StateKind.other || stateKind === StateKind.broken) {
      note = (rest as BrokenEntry).note
    }

    await this.dynamoClient.send(
      new PutItemCommand({
        TableName: config.DB_TABLE_NAME,
        Item: {
          pk: { S: id },
          sk: { S: formatIso8601Date(date) },
          stationId: { S: stationId },
          fieldId: { S: fieldId },
          state: { BOOL: state },
          stateKind: stateKind ? { S: stateKind } : { NULL: true },
          required: required ? { N: `${required}` } : { NULL: true },
          amount: amount ? { N: `${amount}` } : { NULL: true },
          note: note ? { S: note } : { NULL: true },
          crew: crew ? { S: crew } : { NULL: true },
          date: { S: date.toISOString() },
          name: { S: name },
        },
      }),
    );

    return id;
  }

  async getStationEntries(stationId: string, date: string): Promise<Entry[]> {
    const result = await this.dynamoClient.send(new QueryCommand({
      TableName: config.DB_TABLE_NAME,
      IndexName: config.DB_GSI_STATION_DATE,
      KeyConditionExpression: 'sk = :date and stationId = :stationId',
      ExpressionAttributeValues: {
        ':date': { S: date },
        ':stationId': { S: stationId },
      },
    }))
    if (result.Items === undefined) {
      return []
    }

    return result.Items.map(item => recordToEntry(stationId, item)).filter((i): i is Entry => i !== undefined)
  }
}

const recordToEntry = (stationId: string, item: Record<string, AttributeValue>): Entry | undefined => {
  const fieldId = item.fieldId?.S
  const name = item.name?.S
  const state = item.state?.BOOL
  const date = item.date?.S ? parseIso8601Date(item.date?.S) : undefined

  if (!fieldId || !state || !date || !name) { return undefined }

  const amount = item.amount?.N ? parseInt(item.amount?.N, 10) : undefined
  const required = item.required?.N ? parseInt(item.required?.N, 10) : undefined
  const stateKind = parseStateKind(item.stateKind?.S)

  return ({
    crew: item.crew?.S,
    note: item.note?.S,
    fieldId,
    stationId,
    name,
    date,
    required,
    amount,
    state,
    stateKind,
  })
}

export enum StateKind {
  broken = 'broken',
  tooLittle = 'tooLittle',
  other = 'other',
}

const parseStateKind = (stateKind: string | undefined): StateKind | undefined => {
  switch (stateKind) {
    case 'broken':
      return StateKind.broken
    case 'tooLittle':
      return StateKind.tooLittle
    case 'other':
      return StateKind.other
    default:
      return undefined
  }
}

export const entryRepository: EntryRepository = new EntryRepositoryImpl(dynamoClient);
