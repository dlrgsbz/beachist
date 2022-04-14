import { AuthService, useAuthService } from './AuthServiceContext'
import React, { useCallback, useLayoutEffect, useMemo, useState } from 'react'

import Loading from '../components/Loading'
import { UserInfo } from '../dtos'
import { getUserData } from 'modules/data'
import { useIsMounted } from 'lib'

export interface AuthContextType {
  user: UserInfo | null
  logout: () => Promise<void>
  login: (name: string, password: string) => Promise<void>
  tokenLogin: (token: string) => Promise<void>
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any,@typescript-eslint/no-unused-vars
const asyncNoop: (...args: any[]) => any = async (..._: any[]) => {}

const AuthContext = React.createContext<AuthContextType>({
  user: null,
  login: asyncNoop,
  logout: asyncNoop,
  tokenLogin: asyncNoop,
})
AuthContext.displayName = 'AuthContext'

const getLoginState = async (authService: AuthService): Promise<UserInfo | null> => {
  const loginState = authService.isLoggedIn
  let userData = null

  if (loginState) {
    userData = getUserData(authService.getAndValidateToken())
  }

  return userData
}

export const AuthProvider: React.FC = props => {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [loading, setLoading] = useState<boolean>(false)
  const [idle, setIdle] = useState<boolean>(true)
  const mounted = useIsMounted()
  const authService = useAuthService()

  useLayoutEffect(() => {
    if (!mounted) {
      return
    }
    setLoading(true)
    setIdle(false)

    getLoginState(authService)
      .then(userData => {
        if (mounted) {
          setUser(userData)
        }
      })
      .catch(e => {
        if (mounted) {
          console.log('error', e)
        }
      })
      .finally(() => {
        if (mounted) {
          setLoading(false)
        }
      })
  }, [authService, mounted])

  const login = useCallback(
    (email: string, password: string) => authService.login(email, password).then(userData => setUser(userData)),
    [authService],
  )

  const tokenLogin = useCallback(
    (token: string) => authService.tokenLogin(token).then(userData => setUser(userData)),
    [authService],
  )

  const logout = useCallback(() => authService.logout().then(() => setUser(null)), [authService])

  const value = useMemo(
    () => ({
      user,
      login,
      logout,
      tokenLogin,
    }),
    [tokenLogin, login, logout, user],
  )

  if (loading || idle) {
    return <Loading />
  }

  return <AuthContext.Provider value={value} {...props} />
}

export const useAuth = () => {
  const context = React.useContext(AuthContext)
  if (context === undefined) {
    throw new Error(`useAuth must be used within a AuthProvider`)
  }
  return context
}
