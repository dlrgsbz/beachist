import { BrowserRouter as Router, Redirect, Route } from 'react-router-dom'
import Navigation from './components/Navigation'
import Wachfuehrer from './pages/Wachfuehrer'
import { Footer } from './components/Footer'
import React from 'react'

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
          <Navigation/>
        </header>
        <div className="container-fluid">
          <div className="row">
            <main role="main" className="col-md-12">
              <div id="content">
                <Route path="/" exact component={RedirectComponent}/>
                <Route path="/wachfuehrer/" component={Wachfuehrer}/>
                {/*<Route path="/admin/" component={Admin}/>*/}
              </div>
              <Footer/>
            </main>
          </div>
        </div>
      </div>
    </Router>
  )
}