import { Moment } from 'moment'

export interface MqttMessage {
  id: number
  topic: string
  payload: string
  // Currently always string
  // payloadType: PayloadType
  qos: number
  retain: boolean
  ts: Moment
}
