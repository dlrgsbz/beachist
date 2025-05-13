import React from 'react'
import { MqttMessage } from '../../../dtos/mqtt'

interface MqttMessageRowProps {
  message: MqttMessage
}

export const MqttMessageRow: React.VFC<MqttMessageRowProps> = ({ message }) => {
  const { ts, topic, payload, qos, retain } = message
  const retainString = retain ? '♻️' : '❌'
  const tsString = ts.format('L LTS')

  return (
    <tr>
      <td>{tsString}</td>
      <td>{topic}</td>
      <td>{payload}</td>
      <td>{qos}</td>
      <td>{retainString}</td>
    </tr>
  )
}
