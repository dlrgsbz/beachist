import React from 'react'
import { useObserver } from 'mobx-react-lite'
import { useTranslation } from 'react-i18next'
import { useAdminStore } from 'store'
import Legend from '../../Legend'
import { WachfuehrerTurmDetail } from '../../WachfuehrerTurmDetail'

const StationInfo: React.FC = () => {
  const adminStore = useAdminStore()

  const { t } = useTranslation()

  return useObserver(() => (
    <>
      <p>EH-Leistungen heute: {adminStore.firstAid}</p>
      <p>Suchmeldungen heute: {adminStore.search}</p>

      <Legend />

      <div className="accordion">
        {adminStore.stations.map(station => (
          <WachfuehrerTurmDetail
            key={station.id}
            title={station.name}
            stationState={adminStore.stationState(station.id)}
            crew={adminStore.crews.get(station.id)}
            onlineStateSince={station.onlineStateSince}
            isOnline={station.online}
          >
            {adminStore
              .stationEntries(station.id)
              .filter(entry => !entry.state)
              .map(entry => (
                <li>
                  {entry.field.name}: {entry.stateKind && t('statekind_' + entry.stateKind.toString())} (
                  {entry.stateKind === 'tooLittle' && (entry.amount ?? 0) + ' noch vorhanden'}
                  {entry.stateKind === 'broken' && entry.note}
                  {entry.stateKind === 'other' && entry.note})
                </li>
              ))}
          </WachfuehrerTurmDetail>
        ))}
      </div>
    </>
  ))
}

export default StationInfo
