import { Navigate, Outlet } from 'react-router'

import { Footer } from 'components/Footer'
import Navigation from 'components/Navigation'
import { useAuth } from 'context'

export default function ProtectedLayout() {
  const { user } = useAuth()

  if (!user) {
    return <Navigate to="/" replace />
  }

  return (
    <div className="container">
      <header className="App-header">
        <Navigation />
      </header>
      <div className="container-fluid">
        <div className="row">
          <main role="main" className="col-md-12">
            <div id="content">
              <Outlet />
            </div>
            <Footer />
          </main>
        </div>
      </div>
    </div>
  )
}
