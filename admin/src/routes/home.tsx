import { Login } from 'pages/Login'
import { Navigate } from 'react-router'
import { useAuth } from 'context'

export default function Home() {
  const { user } = useAuth()

  return user ? <Navigate to="/wachfuehrer" replace /> : <Login />
}
