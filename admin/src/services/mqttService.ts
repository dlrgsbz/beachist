import { ApiClient } from '../modules/data'
import moment from 'moment'
import { MqttMessage } from '../dtos/mqtt'
import { ApiMqttMessage } from '../modules/data/dtos'

export class MqttService {
  constructor(private apiClient: ApiClient) {}

  public async getMqttList(): Promise<MqttMessage[]> {
    const data = await this.apiClient.fetchMqttMessages()
    return data.map(mapMqttMessage)
  }
}

const mapMqttMessage = (message: ApiMqttMessage): MqttMessage => ({
  ...message,
  ts: moment(message.ts),
})
