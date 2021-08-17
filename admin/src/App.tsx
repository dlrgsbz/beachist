import React from 'react'
import moment from 'moment'
import 'moment/locale/de'
import { Login } from 'pages/Login'
import { useAuth } from './context'
import { AuthenticatedApp } from './AuthenticatedApp'

moment.locale('de')

function App() {
  const { user } = useAuth()

  return user ? <AuthenticatedApp/> : <Login/>
}

export default App
