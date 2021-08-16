import { useConstant } from 'lib'
import React from 'react'
import jwtDecode from 'jwt-decode'

import { UserInfo } from '../dtos'
import { getUserData, login } from 'modules/data'
import { getToken, removeTokens, setToken } from '../lib/token'

export class AuthService {
  public async login(username: string, password: string): Promise<UserInfo> {
    const { name, description, token } = await login(username, password)
    setToken(token)
    return { name, description }
  }

  public async getLoggedInUser(): Promise<UserInfo> {
    let token = ''

    try {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
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
    let token = getToken()

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

export const AuthServiceProvider: React.FC<any> = props => {
  const authService = useConstant<AuthService>(() => new AuthService())

  return <AuthServiceContext.Provider value={authService} {...props} />
}

export const useAuthService = () => React.useContext(AuthServiceContext)

function isTokenExpired(token: string) {
  const { exp } = jwtDecode<{ exp: number }>(token)
  return exp * 1000 < Date.now()
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
function isLoginTokenValid(token: string | null): boolean {
  if (!token) {
    return false
  }

  return !isTokenExpired(token)
}

export class AccessTokenNotFound extends Error {
}
