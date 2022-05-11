import React from 'react'
import 'moment/locale/de'
import { AuthProvider, useAuth } from './context'
import { AuthenticatedApp } from './AuthenticatedApp'
import { UnauthenticatedApp } from './UnauthenticatedApp'
import { AuthServiceProvider } from './context/AuthServiceContext'
import { SnackbarProvider } from 'notistack'
import StoreProvider from 'store'
import DateAdapter from '@mui/lab/AdapterMoment'
import { LocalizationProvider } from '@mui/lab'

const AppRender = () => {
  const { user } = useAuth()

  return user ? <AuthenticatedApp /> : <UnauthenticatedApp />
}

const App = () => (
  <LocalizationProvider dateAdapter={DateAdapter}>
    <SnackbarProvider>
      <AuthServiceProvider>
        <AuthProvider>
          <StoreProvider>
            <AppRender />
          </StoreProvider>
        </AuthProvider>
      </AuthServiceProvider>
    </SnackbarProvider>
  </LocalizationProvider>
)

export default App
