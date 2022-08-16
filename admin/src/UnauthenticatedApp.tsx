import { Navigate, Route, BrowserRouter as Router, Routes } from 'react-router-dom'

import { Login } from './pages/Login'
import React from 'react'
import { UrlLogin } from './pages/UrlLogin'

const RedirectComponent = () => <Navigate to="/" replace />

export const UnauthenticatedApp: React.VFC = () => {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<UrlLogin />} />
        <Route path="/" element={<Login />} />
        <Route path="*" element={<RedirectComponent />} />
      </Routes>
    </Router>
  )
}
