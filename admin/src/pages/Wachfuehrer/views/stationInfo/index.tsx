import Legend from '../../Legend'
import React from 'react'
import { StateKind } from '../../../../dtos'
import { WachfuehrerTurmDetail } from '../../WachfuehrerTurmDetail'
import { useDashboardStore } from 'store'
import { useObserver } from 'mobx-react-lite'

const mapStateKindToString = (stateKind: StateKind) => {
  switch (stateKind) {
    case 'other':
      return 'andere'
    case 'tooLittle':
      return 'zu wenig'
    case 'broken':
      return 'defekt'
  }
}

const StationInfo: React.FC = () => {
  const adminStore = useDashboardStore()

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
                <li key={entry.id}>
                  {entry.field.name}: {entry.stateKind && mapStateKindToString(entry.stateKind)} (
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
