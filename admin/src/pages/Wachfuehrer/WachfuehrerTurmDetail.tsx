import './WachfuehrerTurmDetail.scss'

import { ConnectedIcon, DisconnectedIcon, SquareCheck, SquareEmpty, SquareX } from './components'
import React, { useState } from 'react'

import { Moment } from 'moment'
import { StationState } from 'interfaces'
import classNames from 'classnames'
import { useGeneratedId } from 'lib'

interface WachfuehrerTurmDetailProps {
  title: string
  stationState: StationState
  crew?: string
  isOnline?: boolean
  onlineStateSince?: Moment
}

interface TitleProps {
  title: string
  stationState: StationState
  isVisible: boolean
  setVisible: (newValue: boolean) => void
  bodyId: string
}

interface OnlineStatusProps {
  isOnline?: boolean
  onlineStateSince?: Moment
}

const Title: React.FC<TitleProps> = ({ title, isVisible, setVisible, stationState, bodyId }) => {
  if (stationState === StationState.notOkay) {
    return (
      <button
        className="btn btn-link"
        data-toggle="collapse"
        data-target={`#${bodyId}`}
        aria-expanded={isVisible ? 'true' : 'false'}
        aria-controls={bodyId}
        onClick={() => setVisible(!isVisible)}
      >
        <SquareX /> {title}
      </button>
    )
  }
  if (stationState === StationState.missing) {
    return (
      <>
        <SquareEmpty /> {title}
      </>
    )
  }
  return (
    <>
      <SquareCheck /> {title}
    </>
  )
}

export const WachfuehrerTurmDetail: React.FC<WachfuehrerTurmDetailProps> = ({
  title,
  stationState,
  crew,
  isOnline,
  onlineStateSince,
  children,
}) => {
  const [isVisible, setVisible] = useState(false)

  const id = useGeneratedId('card')
  const bodyId = useGeneratedId('cardBody')
  const className = classNames('collapse', { show: isVisible })

  return (
    <div className="card">
      <div className="card-header" id={id} onClick={() => setVisible(!isVisible)}>
        <h5 className="mb-0">
          <Title
            title={title}
            stationState={stationState}
            isVisible={isVisible}
            setVisible={setVisible}
            bodyId={bodyId}
          />
        </h5>
        <OnlineStatus isOnline={isOnline} onlineStateSince={onlineStateSince} />
      </div>
      <div id={bodyId} className={className} aria-labelledby={id} data-parent="#accordion">
        <div className="card-body">
          {crew && <p>Besatzung: {crew}</p>}
          {children && <ul>{children}</ul>}
        </div>
      </div>
    </div>
  )
}

const OnlineStatus: React.VFC<OnlineStatusProps> = ({ onlineStateSince, isOnline }) => {
  const label = isOnline ? 'online' : 'offline'
  const Icon = isOnline ? ConnectedIcon : DisconnectedIcon
  const timeFrame = onlineStateSince?.fromNow(false) ?? 'noch nie gesehen'

  return (
    <div className="online-status">
      <Icon className="online-status-indicator" titleAccess={label} />
      {label} ({timeFrame})
    </div>
  )
}
