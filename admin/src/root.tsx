import 'moment/locale/de'

import { Links, Meta, Outlet, Scripts, ScrollRestoration } from 'react-router'

import { AdapterMoment } from '@mui/x-date-pickers/AdapterMoment'
import { AuthProvider } from 'context'
import { AuthServiceProvider } from 'context/AuthServiceContext'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import type { ReactNode } from 'react'
import { SnackbarProvider } from 'notistack'
import StoreProvider from 'store'
import moment from 'moment'

moment.updateLocale('de', {
  relativeTime: {
    past: 'seit %s',
  },
})

export function Layout({ children }: { children: ReactNode }) {
  return (
    <html lang="de">
      <head>
        <meta charSet="utf-8" />
        <link rel="shortcut icon" href="/favicon.ico" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="stylesheet" href="https://dlrg.net/global/layout/isc/styles/stamm.css" />
        <title>Beachist</title>
        <Meta />
        <Links />
      </head>
      <body id="startseite" className="isc">
        {children}
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  )
}

export default function App() {
  return (
    <LocalizationProvider dateAdapter={AdapterMoment}>
      <SnackbarProvider>
        <AuthServiceProvider>
          <AuthProvider>
            <StoreProvider>
              <Outlet />
            </StoreProvider>
          </AuthProvider>
        </AuthServiceProvider>
      </SnackbarProvider>
    </LocalizationProvider>
  )
}
