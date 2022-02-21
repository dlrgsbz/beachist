import React from 'react'
import moment from 'moment'
import 'moment/locale/de'
import { useAuth } from './context'
import { AuthenticatedApp } from './AuthenticatedApp'
import { UnauthenticatedApp } from './UnauthenticatedApp'
import { AuthProvider } from './context'
import { AuthServiceProvider } from './context/AuthServiceContext'
import { SnackbarProvider } from 'notistack'
import StoreProvider from 'store'

moment.locale('de')

const AppRender = () => {
  const { user } = useAuth()

  return user ? <AuthenticatedApp/> : <UnauthenticatedApp/>
}

const App = () => (
  <SnackbarProvider>
    <AuthServiceProvider>
      <AuthProvider>
        <StoreProvider>
          <AppRender />
        </StoreProvider>
      </AuthProvider>
    </AuthServiceProvider>
  </SnackbarProvider>
)

export default App
