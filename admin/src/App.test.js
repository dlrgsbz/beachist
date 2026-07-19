import React, { act } from 'react'

import App from './App'
import StoreProvider from 'store'
import { createRoot } from 'react-dom/client'

it('renders without crashing', () => {
  const div = document.createElement('div')
  const root = createRoot(div)
  act(() => {
    root.render(
      <StoreProvider>
        <App />
      </StoreProvider>,
    )
  })
  act(() => {
    root.unmount()
  })
})
