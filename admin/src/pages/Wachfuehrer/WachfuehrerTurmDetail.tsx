import React, { useState } from 'react'
import { Color } from '../../interfaces/ui'

interface WachfuehrerTurmDetailProps {
  title: string
  color: Color
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
        🔴 {title}
      </button>
    )
  }
  if (color === Color.yellow) {
    return <>⚠️ {title}</>
  }
  return <>✅ {title}</>
}

export const WachfuehrerTurmDetail: React.FC<WachfuehrerTurmDetailProps> = ({ title, color, children }) => {
  const [isVisible, setVisible] = useState(false)

  let visible = isVisible
  if (color === Color.green || color === Color.yellow) {
    visible = false
  }

  return (
    <div className="card">
      <div className="card-header" id="headerStation1" onClick={() => setVisible(!isVisible)}>
        <h5 className="mb-0">
          <Title title={title} color={color} isVisible={visible} setVisible={setVisible} />
        </h5>
      </div>
      <div
        id="station1"
        className={'collapse ' + (visible ? 'show' : '')}
        aria-labelledby="headerStation1"
        data-parent="#accordion"
      >
        {children && (
          <div className="card-body">
            <ul>{children}</ul>
          </div>
        )}
      </div>
    </div>
  )
}
