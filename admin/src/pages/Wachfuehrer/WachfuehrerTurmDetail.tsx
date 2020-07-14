import React, { useState } from 'react'
import { Color } from 'interfaces'

interface WachfuehrerTurmDetailProps {
  title: string
  color: Color
  crew?: string
  children?: React.ReactNode
}

interface TitleProps {
  title: string
  color: Color
  isVisible: boolean
  setVisible: (arg: boolean) => void
}

const Title: React.FC<TitleProps> = ({ title, isVisible, setVisible, color }) => {
  if (color === Color.red) {
    return (
      <button
        className="btn btn-link"
        data-toggle="collapse"
        data-target="#station1"
        aria-expanded={isVisible ? 'true' : 'false'}
        aria-controls="station1"
        onClick={() => setVisible(!isVisible)}
      >
          <span role="img" aria-label="Not okay">🔴</span> {title}
      </button>
    )
  }
  if (color === Color.yellow) {
      return <><span role="img" aria-label="Not checked">⚠️</span> {title}</>
  }
    return <><span role="img" aria-label="Okay">✅</span> {title}</>
}

export const WachfuehrerTurmDetail: React.FC<WachfuehrerTurmDetailProps> = ({ title, color, crew,children }) => {
  const [isVisible, setVisible] = useState(false)

  return (
    <div className="card">
      <div className="card-header" id="headerStation1" onClick={() => setVisible(!isVisible)}>
        <h5 className="mb-0">
          <Title title={title} color={color} isVisible={isVisible} setVisible={setVisible} />
        </h5>
      </div>
      <div
        id="station1"
        className={'collapse ' + (isVisible ? 'show' : '')}
        aria-labelledby="headerStation1"
        data-parent="#accordion"
      >
          <div className="card-body">
            {crew && <p>Besatzung: {crew}</p>}
            {children && (
              <ul>{children}</ul>
            )}
          </div>
      </div>
    </div>
  )
}
