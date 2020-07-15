import React, { useEffect, useState } from 'react'
import 'react-dates/initialize'
import 'react-dates/lib/css/_datepicker.css'
import { useObserver } from 'mobx-react-lite'
import moment from 'moment'
import { SingleDatePicker } from 'react-dates'
import { useTranslation } from 'react-i18next'
import { WachfuehrerTurmDetail } from './WachfuehrerTurmDetail'
import { useAdminStore } from 'store'
import Loading from 'components/Loading'
import Legend from './Legend'

const Wachfuehrer: React.FC = () => {
  const [isFocused, setFocused] = useState(false)
  const onFocusChange = ({ focused }: { focused: boolean | null }) => setFocused(!!focused)

  const adminStore = useAdminStore()

  const { t } = useTranslation()

  useEffect(() => {
    adminStore.reloadData()
  }, [adminStore])

  const date = adminStore.selectedDate
  const onDateChange = adminStore.changeSelectedDate

  return useObserver(() => (
    <div>
      <h1>Wachführer</h1>
      {adminStore.loading && <Loading/>}
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

        <p>EH-Leistungen heute: {adminStore.firstAid}</p>
        <p>Suchmeldungen heute: {adminStore.search}</p>

        <Legend/>

        <div className="accordion">
          {adminStore.stations.map(station => (
            <WachfuehrerTurmDetail title={station.name} color={adminStore.color(station.id)}
                                   crew={adminStore.crews.get(station.id)}>
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
      </div>
    </div>
  ))
}

export default Wachfuehrer
