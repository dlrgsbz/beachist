import React from 'react'
import ReactDOM from 'react-dom'
import App from './App'
import StoreProvider from 'store'

it('renders without crashing', () => {
  const div = document.createElement('div')
  const app = <StoreProvider><App /></StoreProvider>
  ReactDOM.render(app, div)
  ReactDOM.unmountComponentAtNode(div)
})
