import React, { useEffect } from 'react'
import { TextField, TextFieldProps } from '@mui/material'

import { AdminView } from 'interfaces'
import { ReactComponent as CheckedBox } from './img/done.svg'
import { DatePicker } from '@mui/x-date-pickers/DatePicker'
import { ReactComponent as EmptyCheckbox } from './img/checkbox.svg'
import Loading from 'components/Loading'
import { SpecialEventType } from 'dtos'
import SpecialEvents from './views/specialEvents'
import StationInfo from './views/stationInfo'
import classNames from 'classnames'
import moment from 'moment'
import { useDashboardStore } from 'store'
import { useObserver } from 'mobx-react-lite'

const useStores = () => {
  const dashboardStore = useDashboardStore()

  return useObserver(() => ({
    specialEventsLoading: dashboardStore.specialEvents.status === 'pending',
    specialEvents: dashboardStore.specialEvents.data,
    reloadData: dashboardStore.reloadData,
    date: dashboardStore.selectedDate,
    onDateChange: dashboardStore.changeSelectedDate,
    autoUpdateEnabled: dashboardStore.autoUpdateEnabled,
    toggleAutoUpdate: dashboardStore.toggleAutoUpdate,
    view: dashboardStore.view,
    showStationInfo: dashboardStore.showStationInfo,
    showDamages: dashboardStore.showDamages,
    showSpecialEvents: dashboardStore.showSpecialEvents,
  }))
}

const Wachfuehrer: React.FC = () => {
  const {
    specialEvents: { damage: damages, special: specialEvents },
    reloadData,
    date,
    onDateChange,
    autoUpdateEnabled,
    toggleAutoUpdate,
    view,
    showStationInfo,
    showDamages,
    showSpecialEvents,
  } = useStores()

  useEffect(() => {
    reloadData()
  }, [reloadData])

  return useObserver(() => (
    <div>
      <h1>Wachführer*innen-Dashboard</h1>
      <div>
        <DatePicker
          label="Datum"
          onChange={onDateChange}
          maxDate={moment().endOf('day')}
          value={date}
          renderInput={(params: TextFieldProps) => <TextField {...params} />}
        />
        <div className="btn-group-toggle float-sm-right" data-toggle="buttons">
          {
            <label className={classNames('btn btn-primary', { active: autoUpdateEnabled })}>
              <input
                type="checkbox"
                checked={autoUpdateEnabled}
                autoComplete="off"
                onChange={toggleAutoUpdate}
              />
              {autoUpdateEnabled ? <CheckedBox /> : <EmptyCheckbox />}
              &nbsp;Automatisch aktualisieren
            </label>
          }
        </div>

        <div className="card mt-3">
          <div className="card-header">
            <ul className="nav nav-tabs card-header-tabs">
              <li className="nav-item">
                <button
                  onClick={showStationInfo}
                  className={classNames({ 'nav-link': true, active: view === AdminView.stations })}
                >
                  Stations-Info
                </button>
              </li>
              <li className="nav-item">
                <button
                  onClick={showDamages}
                  className={classNames({ 'nav-link': true, active: view === AdminView.damages })}
                >
                  Schadenmeldungen{' '}
                  {damages.length > 0 && (
                    <span className="badge badge-danger">{damages.length}</span>
                  )}
                </button>
              </li>
              <li className="nav-item">
                <button
                  onClick={showSpecialEvents}
                  className={classNames({ 'nav-link': true, active: view === AdminView.specialEvents })}
                >
                  Besondere Vorkommnisse{' '}
                  {specialEvents.length > 0 && (
                    <span className="badge badge-warning">{specialEvents.length}</span>
                  )}
                </button>
              </li>
            </ul>
          </div>

          <div className="card-body">
            {view === AdminView.stations && <StationInfo />}
            {view === AdminView.damages && <SpecialEvents type={SpecialEventType.damage} />}
            {view === AdminView.specialEvents && <SpecialEvents type={SpecialEventType.event} />}
          </div>
        </div>
      </div>
    </div>
  ))
}

export default Wachfuehrer
