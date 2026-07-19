import { MemoryRouter, Route, Routes } from 'react-router'
import { expect, test } from 'vitest'

import { AdapterMoment } from '@mui/x-date-pickers/AdapterMoment'
import { AuthProvider } from 'context'
import { AuthServiceProvider } from 'context/AuthServiceContext'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { SnackbarProvider } from 'notistack'
import StoreProvider from 'store'
import { act } from 'react'
import { createRoot } from 'react-dom/client'

test('renders app providers and router without crashing', () => {
  const div = document.createElement('div')
  const root = createRoot(div)

  act(() => {
    root.render(
      <LocalizationProvider dateAdapter={AdapterMoment}>
        <SnackbarProvider>
          <AuthServiceProvider>
            <AuthProvider>
              <StoreProvider>
                <MemoryRouter>
                  <Routes>
                    <Route path="/" element={<div>ok</div>} />
                  </Routes>
                </MemoryRouter>
              </StoreProvider>
            </AuthProvider>
          </AuthServiceProvider>
        </SnackbarProvider>
      </LocalizationProvider>,
    )
  })

  expect(div).toBeTruthy()

  act(() => {
    root.unmount()
  })
})
