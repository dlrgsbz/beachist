import React, { useCallback, useEffect } from 'react'
import { useHistory, useLocation } from 'react-router-dom'
import { useAuth } from 'context'
import { useSnackbar } from 'lib'

const useUrlLogin = () => {
  const history = useHistory()
  const { tokenLogin } = useAuth()
  const { errorSnackbar, successSnackbar } = useSnackbar()
  const map = useParseLocationHash()

  return useEffect(() => {
    const token = map['token']

    tokenLogin(token)
      .then(() => {
        successSnackbar('Erfolgreich angemeldet.')
        history.replace('/wachfuehrer')
      })
      .catch(() => {
        history.replace('/')
        errorSnackbar('Ungültiger Login-Link.', { autoHideDuration: 30000 })
      })
  }, [errorSnackbar, history, map, successSnackbar, tokenLogin])
}

const useParseLocationHash = () => {
  const location = useLocation()

  return useCallback(() => {
    let hash = location.hash
    if (hash.substr(0, 1) === '#') {
      hash = hash.substr(1)
    }

    const items = hash.split('&')
    return items.reduce((prev, item) => {
      const split = item.split('=')
      if (split.length === 0) {
        return prev
      }
      const key = split.reverse().pop()
      prev[key!] = split.join('=')
      return prev
    }, {} as Record<string, string>)
  }, [location.hash])()
}

export const UrlLogin: React.VFC = () => {
  useUrlLogin()

  return <></>
}
