import 'moment/locale/de'

import { AuthProvider, useAuth } from './context'

import { AdapterMoment } from '@mui/x-date-pickers/AdapterMoment'
import { AuthServiceProvider } from './context/AuthServiceContext'
import { AuthenticatedApp } from './AuthenticatedApp'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import React from 'react'
import { SnackbarProvider } from 'notistack'
import StoreProvider from 'store'
import { UnauthenticatedApp } from './UnauthenticatedApp'

const AppRender = () => {
  const { user } = useAuth()

  return user ? <AuthenticatedApp /> : <UnauthenticatedApp />
}

const App = () => (
  <LocalizationProvider dateAdapter={AdapterMoment}>
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
