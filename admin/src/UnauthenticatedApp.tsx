import React from 'react'
import { BrowserRouter as Router, Route } from 'react-router-dom'
import { Login } from './pages/Login'
import { UrlLogin } from './pages/UrlLogin'

export const UnauthenticatedApp: React.VFC = () => {
  return <Router>
    <Route path="/login" component={UrlLogin}/>
    <Route path="/" component={Login}/>
  </Router>
}