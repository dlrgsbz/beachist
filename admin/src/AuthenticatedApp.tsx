import { Redirect, Route, BrowserRouter as Router } from 'react-router-dom'

import { AdminStation } from './pages/AdminStation'
import { Footer } from './components/Footer'
import Navigation from './components/Navigation'
import React from 'react'
import Wachfuehrer from './pages/Wachfuehrer'

const RedirectComponent = () => (
  <Redirect
    to={{
      pathname: '/wachfuehrer',
    }}
  />
)

export const AuthenticatedApp: React.VFC = () => {
  return (
    <Router>
      <div className="container">
        <header className="App-header">
          <Navigation />
        </header>
        <div className="container-fluid">
          <div className="row">
            <main role="main" className="col-md-12">
              <div id="content">
                <Route path="/" exact component={RedirectComponent} />
                <Route path="/wachfuehrer/" component={Wachfuehrer} />
                <Route path="/admin/station" component={AdminStation} />
              </div>
              <Footer />
            </main>
          </div>
        </div>
      </div>
    </Router>
  )
}
