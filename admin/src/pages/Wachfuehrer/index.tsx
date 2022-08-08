import React, { useEffect } from 'react'
import { TextField, TextFieldProps } from '@mui/material'

import { AdminView } from 'interfaces'
import { ReactComponent as CheckedBox } from './img/done.svg'
import DatePicker from '@mui/lab/DatePicker'
import { ReactComponent as EmptyCheckbox } from './img/checkbox.svg'
import Loading from 'components/Loading'
import { SpecialEventType } from 'dtos'
import SpecialEvents from './views/specialEvents'
import StationInfo from './views/stationInfo'
import classNames from 'classnames'
import moment from 'moment'
import { useDashboardStore } from 'store'
import { useObserver } from 'mobx-react-lite'

const Wachfuehrer: React.FC = () => {
  const dashboardStore = useDashboardStore()

  useEffect(() => {
    dashboardStore.reloadData()
  }, [dashboardStore])

  const date = dashboardStore.selectedDate
  const onDateChange = dashboardStore.changeSelectedDate

  return useObserver(() => (
    <div>
      <h1>Wachführer*innen-Dashboard</h1>
      {dashboardStore.loading && <Loading />}
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
            <label className={classNames('btn btn-primary', { active: dashboardStore.autoUpdateEnabled })}>
              <input
                type="checkbox"
                checked={dashboardStore.autoUpdateEnabled}
                autoComplete="off"
                onChange={dashboardStore.toggleAutoUpdate}
              />
              {dashboardStore.autoUpdateEnabled ? <CheckedBox /> : <EmptyCheckbox />}
              &nbsp;Automatisch aktualisieren
            </label>
          }
        </div>

        <div className="card mt-3">
          <div className="card-header">
            <ul className="nav nav-tabs card-header-tabs">
              <li className="nav-item">
                <button
                  onClick={dashboardStore.showStationInfo}
                  className={classNames({ 'nav-link': true, active: dashboardStore.view === AdminView.stations })}
                >
                  Stations-Info
                </button>
              </li>
              <li className="nav-item">
                <button
                  onClick={dashboardStore.showDamages}
                  className={classNames({ 'nav-link': true, active: dashboardStore.view === AdminView.damages })}
                >
                  Schadenmeldungen{' '}
                  {dashboardStore.damages.length > 0 && (
                    <span className="badge badge-danger">{dashboardStore.damages.length}</span>
                  )}
                </button>
              </li>
              <li className="nav-item">
                <button
                  onClick={dashboardStore.showSpecialEvents}
                  className={classNames({ 'nav-link': true, active: dashboardStore.view === AdminView.specialEvents })}
                >
                  Besondere Vorkommnisse{' '}
                  {dashboardStore.specialEvents.length > 0 && (
                    <span className="badge badge-warning">{dashboardStore.specialEvents.length}</span>
                  )}
                </button>
              </li>
            </ul>
          </div>

          <div className="card-body">
            {dashboardStore.view === AdminView.stations && <StationInfo />}
            {dashboardStore.view === AdminView.damages && <SpecialEvents type={SpecialEventType.damage} />}
            {dashboardStore.view === AdminView.specialEvents && <SpecialEvents type={SpecialEventType.event} />}
          </div>
        </div>
      </div>
    </div>
  ))
}

export default Wachfuehrer
