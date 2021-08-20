import React from 'react'
import moment from 'moment'
import 'moment/locale/de'
import { useAuth } from './context'
import { AuthenticatedApp } from './AuthenticatedApp'
import { UnauthenticatedApp } from './UnauthenticatedApp'

moment.locale('de')

function App() {
  const { user } = useAuth()

  return user ? <AuthenticatedApp/> : <UnauthenticatedApp/>
}

export default App
