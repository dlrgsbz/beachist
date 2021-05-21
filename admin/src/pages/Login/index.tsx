import React, { useEffect } from 'react'
import './index.scss'
import { useAuthStore } from 'store'
import Loading from 'components/Loading'

export const Login = () => {
  const { availableUsers, loadUsers } = useAuthStore()

  useEffect(() => {
    loadUsers()
  }, [])

  if (availableUsers.status !== 'success') {
    return <Loading />
  }

  return <div className="login--container">
    <div className="login--container-image">
    </div>
    <div className="login--container-form-container">
      <div className="login--form-container">
        <h5>Anmeldung im Wachmanager</h5>
        <p className="text-muted">Willkommen zurück. Bitte gib dein Passwort ein, um dich anzumelden.</p>
        <form>
          <div className="form-group">
            <label htmlFor="exampleFormControlSelect1">Nutzer</label>
            <select className="form-control" id="exampleFormControlSelect1">
              <option>Wachführer</option>
              <option>Wulu</option>
            </select>
            <small id="emailHelp" className="form-text text-muted">Bitte auswählen.</small>
          </div>

          <div className="form-group">
            <label htmlFor="exampleInputPassword1">Passwort</label>
            <input type="password" className="form-control" id="exampleInputPassword1" />
          </div>
          <div className="login--container--form--button-container">
            <button type="submit" className="btn btn-primary">Anmelden</button>
          </div>
        </form>
      </div>
    </div>
  </div>
}
