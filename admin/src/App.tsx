import React from 'react'
import Navigation from 'Navigation'
import { BrowserRouter as Router, Redirect, Route } from 'react-router-dom'
import { Footer } from 'components/Footer'
import Wachfuehrer from 'pages/Wachfuehrer'
import moment from 'moment'
import 'moment/locale/de'
import { Login } from 'pages/Login'

moment.locale('de')

const RedirectComponent = () => (
  <Redirect
    to={{
      pathname: '/wachfuehrer',
    }}
  />
)

function App() {
  return <Login/>;

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
                {/*<Route path="/admin/" component={Admin}/>*/}
              </div>
              <Footer />
            </main>
          </div>
        </div>
      </div>
    </Router>
  )
}

export default App
