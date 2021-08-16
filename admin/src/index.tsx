import React from 'react'
import ReactDOM from 'react-dom'
import App from './App'
import * as serviceWorker from './serviceWorker'
import StoreProvider from 'store'

import i18n from 'modules/i18n'
import moment from 'moment'
import { AuthProvider } from './context'
import { AuthServiceProvider } from './context/AuthServiceContext'
import { SnackbarProvider } from 'notistack'

moment.locale(i18n.language)

ReactDOM.render(
  <SnackbarProvider>
    <AuthServiceProvider>
      <AuthProvider>
        <StoreProvider>
          <App/>
        </StoreProvider>
      </AuthProvider>
    </AuthServiceProvider>
  </SnackbarProvider>,
  document.getElementById('root'),
)

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister()
