import React, { useEffect } from 'react'
import { useSnackbar } from '../../lib'
import { useAdminStore, useMqttStore } from '../../store'
import { useObserver } from 'mobx-react-lite'
import { ProvisioningModal, StationRow } from '../AdminStation/components'
import { SkeletonRow } from '../../components/Skeletons'
import classNames from 'classnames'
import { MqttMessageRow } from './components/MqttMessageRow'

const useStores = () => {
  const mqttStore = useMqttStore()

  return useObserver(() => ({
    loading: mqttStore.messagesState.status === 'pending',
    messages: mqttStore.messagesState.data,
    fetchData: mqttStore.reloadData,
  }))
}

export const MqttView: React.VFC = () => {
  const { loading, messages, fetchData } = useStores()

  useEffect(() => {
    fetchData()
  }, [fetchData])

  const tableClasses = classNames('table', 'table-striped', 'table-no-bordered')

  return (
    <div>
      <h1>MQTT-Debug</h1>

      <table className={tableClasses}>
        <thead>
        <tr>
          <th>Zeitstempel</th>
          <th>Topic</th>
          <th>Message</th>
          <th>QoS</th>
          <th>Retain</th>
        </tr>
        </thead>
        <tbody>
        {loading ? (
          <SkeletonRow rows={3} columns={4} />
        ) : (
          messages.map(message => (
            <MqttMessageRow
              key={message.id}
              message={message}
            />
          ))
        )}
        </tbody>
      </table>
    </div>
  )
}
