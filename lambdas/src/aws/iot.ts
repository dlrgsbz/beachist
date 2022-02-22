import {
  IoTDataPlaneClient,
  PublishCommand,
} from '@aws-sdk/client-iot-data-plane';
import { config } from '../config';

export interface IotClient {
  publish: (topic: string, data: string) => Promise<void>
}

const ioTDataPlaneClient: IoTDataPlaneClient = new IoTDataPlaneClient({
  region: config.AWS_REGION,
  endpoint: config.IOT_DATA_ENDPOINT,
})

class IotClientImpl implements IotClient {
  dataClient: IoTDataPlaneClient

  constructor(dataClient: IoTDataPlaneClient = ioTDataPlaneClient) {
    this.dataClient = dataClient
  }
  async publish(topic: string, data: string): Promise<void> {
    await this.dataClient.send(new PublishCommand({ topic, payload: Buffer.from(data, 'utf-8') }))
  }

}

export const iotClient: IotClient = new IotClientImpl()
