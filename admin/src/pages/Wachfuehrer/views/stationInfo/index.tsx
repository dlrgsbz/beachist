import Legend from '../../Legend'
import React from 'react'
import { WachfuehrerTurmDetail, WachfuehrerTurmDetailSkeleton } from '../../WachfuehrerTurmDetail'
import { useDashboardStore } from 'store'
import { useObserver } from 'mobx-react-lite'
import { useTranslation } from 'react-i18next'

const useStores = () => {
  const dashboardStore = useDashboardStore()

  return useObserver(() => ({
    stationsLoading: dashboardStore.stations.status === 'pending',
    stations: dashboardStore.stations.data,
    crewsLoading: dashboardStore.crews.status === 'pending',
    crews: dashboardStore.crews.data,
    stationOnlineLoading: dashboardStore.stationOnlineState.status === 'pending',
    stationOnlineState: dashboardStore.stationOnlineState.data,
    stationState: dashboardStore.stationState,
    entriesLoading: dashboardStore.entries.status === 'pending',
    stationEntries: dashboardStore.stationEntries,
    firstAid: dashboardStore.firstAid,
    search: dashboardStore.search,
  }))
}

const StationInfo: React.FC = () => {
  const { stationsLoading, entriesLoading, crewsLoading, stations, crews, stationState, stationEntries, firstAid, search, stationOnlineState } = useStores()

  const { t } = useTranslation()

  return useObserver(() => (
    <>
      <p>EH-Leistungen heute: {firstAid}</p>
      <p>Suchmeldungen heute: {search}</p>

      <Legend />

      <div className="accordion">
        {(stationsLoading || entriesLoading) && stations.length === 0 ?
        <>
          <WachfuehrerTurmDetailSkeleton />
          <WachfuehrerTurmDetailSkeleton />
          <WachfuehrerTurmDetailSkeleton />
        </>
        : stations.map(station => (
          <WachfuehrerTurmDetail
            key={station.id}
            title={station.name}
            stationState={stationState(station.id)}
            crew={crews.get(station.id)}
            onlineStateSince={stationOnlineState?.[station.id]?.onlineStateSince}
            isOnline={stationOnlineState?.[station.id]?.online}
          >
            {stationEntries(station.id)
              .filter(entry => !entry.state)
              .map(entry => (
                <li key={entry.id}>
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
