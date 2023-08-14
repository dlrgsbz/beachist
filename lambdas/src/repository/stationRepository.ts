import { DynamoDBClient, GetItemCommand } from '@aws-sdk/client-dynamodb'

import { config } from '../config'
import { dynamoClient } from './dynamoClient'
import { parseIso8601Date } from '../util'

export interface Station {
  id: string
  name: string
  hasSearch: boolean
  online?: boolean
  onlineStateSince?: Date
  appVersion: string
  appVersionCode?: number
  fields: string[]
}

export interface StationRepository {
  getStation(id: string): Promise<Station | undefined>
}

class StationRepositoryImpl implements StationRepository {
  private readonly stationSk = 'station'

  constructor(private readonly dynamoClient: DynamoDBClient) {}

  async getStation(id: string): Promise<Station | undefined> {
    const result = await this.dynamoClient.send(new GetItemCommand({
      TableName: config.DB_TABLE_NAME,
      Key: {
        pk: { S: id },
        sk: { S: this.stationSk },
      },
    }))
    if (result.Item === undefined || !result.Item.name?.S || !result.Item.hasSearch?.BOOL || !result.Item.appVersion?.S) {
      return undefined
    }
    const onlineStateSince = result.Item.onlineStateSince?.S ? parseIso8601Date(result.Item.onlineStateSince.S) : undefined
    const appVersionCode = result.Item.appVersionCode.N ? parseInt(result.Item.appVersionCode.N, 10) : undefined
    const fields = result.Item.fields?.L?.map(i => i.S).filter((i): i is string => i !== undefined) ?? []
    return {
      id,
      name: result.Item.name.S,
      hasSearch: result.Item.hasSearch.BOOL,
      online: result.Item.online.BOOL,
      appVersion: result.Item.appVersion.S,
      onlineStateSince,
      appVersionCode,
      fields,
    }
  }
}

export const stationRepository: StationRepository = new StationRepositoryImpl(dynamoClient)
