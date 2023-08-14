import { DynamoDBClient, GetItemCommand } from '@aws-sdk/client-dynamodb'

import { config } from '../config'
import { dynamoClient } from './dynamoClient'

export interface Field {
  id: string
  name: string
  required?: number
  parent?: string
  note?: string
}

export interface FieldRepository {
  getField(fieldId: string): Promise<Field | undefined>
}

class FieldRepositoryImpl implements FieldRepository {
  private readonly fieldSk = 'field'

  constructor(private readonly dynamoClient: DynamoDBClient) {}

  async getField(id: string): Promise<Field | undefined> {
    const result = await this.dynamoClient.send(new GetItemCommand({
      TableName: config.DB_TABLE_NAME,
      Key: {
        pk: { S: id },
        sk: { S: this.fieldSk },
      },
    }))
    if (result.Item === undefined || !result.Item.name?.S) {
      return undefined
    }
    const required = result.Item.required.N ? parseInt(result.Item.required.N, 10) : undefined
    return {
      id,
      name: result.Item.name.S,
      required,
      parent: result.Item.parent?.S,
      note: result.Item.note?.S,
    }
  }
}

export const fieldRepository = new FieldRepositoryImpl(dynamoClient);
