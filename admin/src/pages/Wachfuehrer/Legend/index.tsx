import React, { useState } from 'react'

const Legend: React.FC = () => {
  const [isVisible, setVisible] = useState(true)

  return (
    <div className="card">
      <div className="card-header" id="headerLegend" onClick={() => setVisible(!isVisible)}>
        <h5 className="mb-0">Legende</h5>
      </div>
      <div
        id="station1"
        className={'collapse ' + (isVisible ? 'show' : '')}
        aria-labelledby="headerLegend"
        data-parent="#accordion"
      >
        <div className="card-body">
          <ul>
            <li>
              <span role="img" aria-label="Not checked">
                ⚠️
              </span>{' '}
              Diese Station wurde noch nicht überprüft
            </li>
            <li>
              <span role="img" aria-label="Not okay">
                🔴
              </span>{' '}
              Min. 1 Punkt der Überprüfung ist nicht in Ordnung
            </li>
            <li>
              <span role="img" aria-label="Okay">
                ✅
              </span>{' '}
              Diese Station wurde überprüft und alles ist in Ordnung
            </li>
          </ul>
        </div>
      </div>
    </div>
  )
}

export default Legend
