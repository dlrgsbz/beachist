import { Link, useNavigate, useParams } from 'react-router'
import React, { useEffect, useMemo } from 'react'

import { AdminView } from 'interfaces'
import CheckedBox from './img/done.svg?react'
import { DatePicker } from '@mui/x-date-pickers/DatePicker'
import EmptyCheckbox from './img/checkbox.svg?react'
import Loading from 'components/Loading'
import { SpecialEventType } from 'dtos'
import SpecialEvents from './views/specialEvents'
import StationInfo from './views/stationInfo'
import { UndoIcon } from './img/UndoIcon'
import classNames from 'classnames'
import moment from 'moment'
import { useDashboardStore } from 'store'
import { useObserver } from 'mobx-react-lite'

const useStores = () => {
  const dashboardStore = useDashboardStore()

  return useObserver(() => ({
    loading: dashboardStore.loading,
    reloadData: dashboardStore.reloadData,
    autoUpdateEnabled: dashboardStore.autoUpdateEnabled,
    toggleAutoUpdate: dashboardStore.toggleAutoUpdate,
    view: dashboardStore.view,
    showStationInfo: dashboardStore.showStationInfo,
    showDamages: dashboardStore.showDamages,
    showSpecialEvents: dashboardStore.showSpecialEvents,
    damages: dashboardStore.damages,
    specialEvents: dashboardStore.specialEvents,
  }))
}

const useCurrentTab = () => {
  const { tab } = useParams()

  switch (tab) {
    case 'info':
      return AdminView.stations
    case 'schaden':
      return AdminView.damages
    case 'besondere-vorkommnisse':
      return AdminView.specialEvents
    default:
      return null
  }
}

const useSelectedDate = (): [moment.Moment | null, (newDate: moment.Moment | null) => void] => {
  const { date, tab } = useParams()
  const navigate = useNavigate()

  const selectedDate = useMemo(() => (date ? moment(date) : null), [date])

  const changeSelectedDate = (newDate: moment.Moment | null) => {
    if (newDate) {
      navigate(`/wachfuehrer/${newDate.format('YYYY-MM-DD')}/${tab}`, { replace: true })
    }
  }

  return [selectedDate, changeSelectedDate]
}

const Wachfuehrer: React.FC = () => {
  const { loading, reloadData, autoUpdateEnabled, toggleAutoUpdate, damages, specialEvents } = useStores()

  const [selectedDate, changeSelectedDate] = useSelectedDate()

  const hasDamages = damages.length > 0
  const hasSpecialEvents = specialEvents.length > 0

  const currentTab = useCurrentTab()
  const navigate = useNavigate()

  const formattedDate = selectedDate ? selectedDate.format('YYYY-MM-DD') : null
  const today = moment().format('YYYY-MM-DD')
  const isToday = selectedDate ? selectedDate.isSame(moment(), 'day') : false

  const resetToToday = () => {
    navigate('/wachfuehrer')
  }

  useEffect(() => {
    if (selectedDate) {
      reloadData(selectedDate)
    }
  }, [reloadData, selectedDate])

  useEffect(() => {
    if (selectedDate === null || currentTab == null) {
      navigate(`/wachfuehrer/${today}/info`, { replace: true })
    }
  }, [currentTab, navigate, selectedDate, today])

  return (
    <div>
      <h1>Wachführer*innen-Dashboard</h1>
      {loading && <Loading />}
      <div>
        <div className="d-flex justify-content-between">
          <div className="d-flex">
            <DatePicker
              label="Datum"
              onChange={changeSelectedDate}
              maxDate={moment().endOf('day')}
              value={selectedDate}
            />
            {!isToday && (
              <button
                onClick={resetToToday}
                className="btn btn-outline-secondary ml-2 h-100 d-flex gap-1 justify-content-center align-items-center"
                // todo: move to utility class
                style={{ gap: '0.5rem' }}
              >
                <UndoIcon />
                Zurück zu heute
              </button>
            )}
          </div>
          <div className="btn-group-toggle float-sm-right" data-toggle="buttons">
            {
              <label className={classNames('btn btn-primary', { active: autoUpdateEnabled })}>
                <input type="checkbox" checked={autoUpdateEnabled} autoComplete="off" onChange={toggleAutoUpdate} />
                {autoUpdateEnabled ? <CheckedBox /> : <EmptyCheckbox />}
                &nbsp;Automatisch aktualisieren
              </label>
            }
          </div>
        </div>

        <div className="card mt-3">
          <div className="card-header">
            <ul className="nav nav-tabs card-header-tabs">
              <li className="nav-item">
                <Link
                  to={`/wachfuehrer/${formattedDate}/info`}
                  className={classNames({ 'nav-link': true, active: currentTab === AdminView.stations })}
                >
                  Stations-Info
                </Link>
              </li>
              <li className="nav-item">
                <Link
                  to={`/wachfuehrer/${formattedDate}/schaden`}
                  className={classNames({ 'nav-link': true, active: currentTab === AdminView.damages })}
                >
                  Schadenmeldungen {hasDamages && <span className="badge badge-danger">{damages.length}</span>}
                </Link>
              </li>
              <li className="nav-item">
                <Link
                  to={`/wachfuehrer/${formattedDate}/besondere-vorkommnisse`}
                  className={classNames({ 'nav-link': true, active: currentTab === AdminView.specialEvents })}
                >
                  Besondere Vorkommnisse{' '}
                  {hasSpecialEvents && <span className="badge badge-warning">{specialEvents.length}</span>}
                </Link>
              </li>
            </ul>
          </div>

          <div className="card-body">
            {currentTab === AdminView.stations && <StationInfo />}
            {currentTab === AdminView.damages && <SpecialEvents type={SpecialEventType.damage} />}
            {currentTab === AdminView.specialEvents && <SpecialEvents type={SpecialEventType.event} />}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Wachfuehrer
