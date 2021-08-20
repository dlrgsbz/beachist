import React, { FunctionComponent, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useNavigationStore } from 'store'
import { useAuth } from 'context'
import { QrButton } from './QrButton'

type NavlinkProps = {
  target: string
}

const Navlink: FunctionComponent<NavlinkProps> = ({ target, children }) => (
  <li className="nav-item">
    <Link className="nav-link" to={target}>
      {children}
    </Link>
  </li>
)

const Navigation = () => {
  const [isExpanded, setExpanded] = useState(false)
  const [currentWachtag, setCurrentWachtag] = useState('')
  const navigationStore = useNavigationStore()

  const { logout } = useAuth()

  useEffect(() => {
    setCurrentWachtag(navigationStore.currentWachtag.format('DD.MM.Y'))
  }, [navigationStore, currentWachtag])

  return (
    <nav className="navbar navbar-expand-lg navbar-dark">
      <a className="navbar-brand" href="/" style={{ color: '#ffed00' }}>
        <svg width="135" height="26" xmlns="http://www.w3.org/2000/svg">
          <g fill="none" fillRule="evenodd">
            <path
              d="M119.467 10.701h15.467v8.38c0 1.617-1.088 3.78-3.346 4.39-2.272.615-4.478.475-6.955.475h-10.332c-3.423.05-6.349.298-8.12-.917-1.332-.913-2.201-2.346-2.18-3.948V6.902c-.024-1.69.947-2.918 2.203-3.936 1.535-1.243 4.72-.931 8.099-.931h10.328c4.079 0 6.768.01 8.267 1.066 2.295 1.615 2.098 4.78 2.098 4.78h-10.382c-.328-1.58-1.244-1.993-3.341-1.993h-3.329c-2.272-.024-3.124.74-3.11 2.065v9.707c0 1.169.172 1.684 1.098 2.125.725.36 1.96.3 3.37.3l2.631.002c2.485 0 2.7-1.338 2.7-2.8v-2.593h-5.166v-3.993M38 24V2h10.85l-.002 16.129H63V24H38M20.152 11v3.967c0 .658.099 2.309-.955 2.748-1.348.563-2.33.382-3.696.403H10.85V7.851h4.658c1.365.022 2.343-.159 3.692.404 1.054.44.951 2.088.952 2.746zm.524 12.976c2.825 0 5.906.228 7.924-.734 2.037-.962 2.395-3.507 2.396-4.832L31 7.616c0-1.324-.358-3.87-2.395-4.833-2.02-.96-5.102-.767-7.927-.767H0v21.961h20.676zM78.912 5.892h4.716c1.494 0 2.64-.117 3.657.348 1.244.568 1.074 1.545 1.075 2.056 0 .495.123 1.553-1.075 2.07-1.081.466-1.87.337-3.657.337l-4.716.001V5.892zM68 24V2.004h20.611c2.385.03 5.076-.19 7.003.694C97.81 3.706 99 4.863 99 7.274l-.016.593c0 1.881-.801 2.86-2.403 3.872-1.582 1.011-3.534 1.157-7.266 1.157h4.468c1.418 0 2.636.242 3.65.73 1.034.477 1.55 1.772 1.55 2.439L98.986 24H88.082l.002-6.964c0-.669.048-1.756-.914-2.056-1.054-.33-1.718-.235-3.407-.266h-4.851V24H68z"
              fill="#FFF101"
            />
          </g>
        </svg>
      </a>
      <button
        className="navbar-toggler"
        type="button"
        data-toggle="collapse"
        data-target="#navbarNav"
        onClick={() => setExpanded(!isExpanded)}
        aria-controls="navbarNav"
        aria-expanded={isExpanded}
        aria-label="Toggle navigation"
      >
        <span className="navbar-toggler-icon"/>
      </button>
      <div className={'collapse navbar-collapse ' + (isExpanded ? 'show' : '')} id="navbarNav">
        <ul className="navbar-nav mr-auto mt-2 mt-lg-0">
          <Navlink target="/">Start</Navlink>
          <Navlink target="/wachfuehrer/">Wachführer*innen-Dashboard</Navlink>
          {/*<Navlink target="/admin/">Admin</Navlink>*/}
        </ul>
        <ul className="nav navbar-nav flex-row justify-content-between ml-auto">
          <QrButton/>
          <li className="nav-item">
            <button className="btn btn-outline-warning btn-sm nav-link d-inline m-0 p-2"
                    onClick={logout} type="button" value="Abmelden"
                    title="Benutzer abmelden">
              Abmelden
            </button>
          </li>
        </ul>
      </div>
    </nav>
  )
}

export default Navigation
