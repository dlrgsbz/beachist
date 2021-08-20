import React, { useEffect, useState } from 'react'
import 'react-dates/initialize'
import 'react-dates/lib/css/_datepicker.css'
import { useObserver } from 'mobx-react-lite'
import moment from 'moment'
import { SingleDatePicker } from 'react-dates'
import classNames from 'classnames'
import { useAdminStore } from 'store'
import Loading from 'components/Loading'
import { AdminView } from 'interfaces'
import { SpecialEventType } from 'dtos'
import StationInfo from './views/stationInfo'
import SpecialEvents from './views/specialEvents'

import { ReactComponent as CheckedBox } from './img/done.svg'
import { ReactComponent as EmptyCheckbox } from './img/checkbox.svg'

const Wachfuehrer: React.FC = () => {
  const [isFocused, setFocused] = useState(false)
  const onFocusChange = ({ focused }: { focused: boolean | null }) => setFocused(!!focused)

  const adminStore = useAdminStore()

  useEffect(() => {
    adminStore.reloadData()
  }, [adminStore])

  const date = adminStore.selectedDate
  const onDateChange = adminStore.changeSelectedDate

  return useObserver(() => (
    <div>
      <h1>Wachführer*innen-Dashboard</h1>
      {adminStore.loading && <Loading />}
      <div>
        <SingleDatePicker
          id="15de9e5a"
          date={date}
          focused={isFocused}
          onDateChange={onDateChange}
          onFocusChange={onFocusChange}
          numberOfMonths={2}
          showDefaultInputIcon={true}
          isOutsideRange={date => date.isAfter(moment().endOf('day'))}
        />
        <div className="btn-group-toggle float-sm-right" data-toggle="buttons">
          {
            <label className={classNames('btn btn-primary', { active: adminStore.autoUpdateEnabled })}>
              <input
                type="checkbox"
                checked={adminStore.autoUpdateEnabled}
                autoComplete="off"
                onChange={adminStore.toggleAutoUpdate}
              />
              {adminStore.autoUpdateEnabled ? <CheckedBox /> : <EmptyCheckbox />}
              &nbsp;Automatisch aktualisieren
            </label>
          }
        </div>

        <div className="card mt-3">
          <div className="card-header">
            <ul className="nav nav-tabs card-header-tabs">
              <li className="nav-item">
                <button
                  onClick={adminStore.showStationInfo}
                  className={classNames({ 'nav-link': true, active: adminStore.view === AdminView.stations })}
                >
                  Stations-Info
                </button>
              </li>
              <li className="nav-item">
                <button
                  onClick={adminStore.showDamages}
                  className={classNames({ 'nav-link': true, active: adminStore.view === AdminView.damages })}
                >
                  Schadenmeldungen{' '}
                  {adminStore.damages.length > 0 && (
                    <span className="badge badge-danger">{adminStore.damages.length}</span>
                  )}
                </button>
              </li>
              <li className="nav-item">
                <button
                  onClick={adminStore.showSpecialEvents}
                  className={classNames({ 'nav-link': true, active: adminStore.view === AdminView.specialEvents })}
                >
                  Besondere Vorkommnisse{' '}
                  {adminStore.specialEvents.length > 0 && (
                    <span className="badge badge-warning">{adminStore.specialEvents.length}</span>
                  )}
                </button>
              </li>
            </ul>
          </div>

          <div className="card-body">
            {adminStore.view === AdminView.stations && <StationInfo />}
            {adminStore.view === AdminView.damages && <SpecialEvents type={SpecialEventType.damage} />}
            {adminStore.view === AdminView.specialEvents && <SpecialEvents type={SpecialEventType.event} />}
          </div>
        </div>
      </div>
    </div>
  ))
}

export default Wachfuehrer
