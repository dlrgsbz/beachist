import { Navigate, Route, BrowserRouter as Router, Routes } from 'react-router-dom'

import { AdminItems } from './pages/AdminItems'
import { AdminStation } from './pages/AdminStation'
import { Footer } from './components/Footer'
import Navigation from './components/Navigation'
import React from 'react'
import Wachfuehrer from './pages/Wachfuehrer'

const RedirectComponent = () => <Navigate to="/wachfuehrer" replace />

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
                <Routes>
                  <Route path="/wachfuehrer" element={<Wachfuehrer />} />
                  <Route path="/wachfuehrer/:date" element={<Wachfuehrer />} />
                  <Route path="/wachfuehrer/:date/:tab" element={<Wachfuehrer />} />
                  <Route path="/admin/station" element={<AdminStation />} />
                  <Route path="/admin/items/*" element={<AdminItems />} />
                  <Route path="/" element={<RedirectComponent />} />
                </Routes>
              </div>
              <Footer />
            </main>
          </div>
        </div>
      </div>
    </Router>
  )
}
