import React from 'react'
import { SpecialEventType } from 'dtos'
import { useDashboardStore } from 'store'
import { useObserver } from 'mobx-react-lite'

type SpecialEventViewProps = {
  type: SpecialEventType
}

const SpecialEvents: React.FC<SpecialEventViewProps> = ({ type }) => {
  const adminStore = useDashboardStore()

  const accessor = type === SpecialEventType.event ? adminStore.specialEvents : adminStore.damages

  return useObserver(() => (
    <>
      {accessor.map(event => (
        <div key={event.id} className="card mb-3">
          <div className="card-header">
            <h5 className="card-title">{event.title}</h5>
          </div>
          <div className="card-body">
            <h6 className="card-title">Beschreibung</h6>
            <div>{event.note}</div>
          </div>
          <div className="card-footer text-muted">
            <div className="row">
              <div className="col">{event.notifier}</div>
              <div className="col text-center">{event.station.name}</div>
              <div className="col text-right">{event.date.format('LT')}</div>
            </div>
          </div>
        </div>
      ))}
    </>
  ))
}

export default SpecialEvents
