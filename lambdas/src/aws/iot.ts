export interface IotClient {
    publish: (topic: string, data: string) => Promise<void>
}

class IotClientImpl implements IotClient {
    async publish(topic: string, data: string): Promise<void> {

    }

}

export const iotClient: IotClient = new IotClientImpl()
