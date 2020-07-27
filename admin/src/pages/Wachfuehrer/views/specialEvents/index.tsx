import React from 'react'
import { useTranslation } from 'react-i18next'
import { useObserver } from 'mobx-react-lite'
import { SpecialEventType } from 'dtos'
import { useAdminStore } from 'store'

type SpecialEventViewProps = {
  type: SpecialEventType
}

const SpecialEvents: React.FC<SpecialEventViewProps> = ({ type }) => {
  const adminStore = useAdminStore()

  const { t } = useTranslation()

  const accessor = type === SpecialEventType.event ? adminStore.specialEvents : adminStore.damages

  return useObserver(() => (
    <>
      {accessor.map(event => (
        <div className="card mb-3">
          <div className="card-header">
            <h5 className="card-title">{event.title}</h5>
          </div>
          <div className="card-body">
            <h6 className="card-title">Beschreibung</h6>
            {event.note}
          </div>
          <div className="card-footer text-muted">
            <div className="row">
              <div className="col">
                {event.notifier}
              </div>
              <div className="col text-center">
                {event.station.name}
              </div>
              <div className="col text-right">
                {event.date.format('LT')}
              </div>
            </div>
          </div>
        </div>
      ))}
    </>
  ))
}

export default SpecialEvents
