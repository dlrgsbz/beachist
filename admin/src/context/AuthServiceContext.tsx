import { getToken, removeTokens, setToken } from '../lib/token'
import { getUserData, login } from 'modules/data'

import React from 'react'
import { UserInfo } from '../dtos'
import jwtDecode from 'jwt-decode'
import { useConstant } from 'lib'

export class AuthService {
  public async login(username: string, password: string): Promise<UserInfo> {
    const { token, ...user } = await login(username, password)
    setToken(token)
    return user
  }

  public async tokenLogin(token: string): Promise<UserInfo> {
    if (!isLoginTokenValid(token)) {
      throw new AccessTokenNotFound('Invalid access token provided')
    }

    const userData = await getUserData(token)
    setToken(token)
    return userData
  }

  public async getLoggedInUser(): Promise<UserInfo> {
    let token = ''

    try {
      token = this.getAndValidateToken()
    } catch (e) {
      if (e instanceof AccessTokenNotFound) {
        removeTokens()
        window.location.replace('/')
      }
    }

    try {
      return getUserData(token)
    } catch (e) {
      throw new Error('Something bad happened')
    }
  }

  public async logout(): Promise<void> {
    removeTokens()
  }

  public get isLoggedIn(): boolean {
    return !!getToken()
  }

  public getAndValidateToken(): string {
    const token = getToken()

    if (!token) {
      throw new AccessTokenNotFound('Could not find valid access token')
    }

    if (isTokenExpired(token)) {
      throw new AccessTokenNotFound('Could not find valid access token')
    }

    return token
  }
}

const AuthServiceContext = React.createContext<AuthService>({} as AuthService)

export const AuthServiceProvider: React.FC = props => {
  const authService = useConstant<AuthService>(() => new AuthService())

  return <AuthServiceContext.Provider value={authService} {...props} />
}

export const useAuthService = () => React.useContext(AuthServiceContext)

function isTokenExpired(token: string) {
  try {
    const { exp } = jwtDecode<{ exp: number }>(token)
    return exp * 1000 < Date.now()
  } catch (e) {
    return true
  }
}

export function isLoginTokenValid(token: string | null): boolean {
  if (!token) {
    return false
  }

  return !isTokenExpired(token)
}

export class AccessTokenNotFound extends Error {}
