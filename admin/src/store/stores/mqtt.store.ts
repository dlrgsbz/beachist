import { action, observable } from 'mobx'
import { MqttService } from 'services/mqttService'
import { MqttMessage } from '../../dtos/mqtt'
import { AsyncState, createAsyncState, runWithAsyncState } from '../../lib'

class MqttStore {
  @observable messagesState: AsyncState<MqttMessage[]> = createAsyncState([])

  constructor(private readonly mqttService: MqttService) {}

  @action.bound
  async reloadData(): Promise<void> {
    await this.fetchMessages()
  }

  @action.bound
  async fetchMessages(): Promise<void> {
    await runWithAsyncState(this.messagesState, () => this.mqttService.getMqttList())
  }
}

export default MqttStore
