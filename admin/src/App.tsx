import 'moment/locale/de'

import { AuthProvider, useAuth } from './context'

import { AuthServiceProvider } from './context/AuthServiceContext'
import { AuthenticatedApp } from './AuthenticatedApp'
import DateAdapter from '@mui/lab/AdapterMoment'
import { LocalizationProvider } from '@mui/lab'
import React from 'react'
import { SnackbarProvider } from 'notistack'
import StoreProvider from 'store'
import { UnauthenticatedApp } from './UnauthenticatedApp'

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
