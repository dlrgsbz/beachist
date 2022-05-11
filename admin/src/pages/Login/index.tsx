import './index.scss'

import React, { useEffect } from 'react'

import { AxiosError } from 'axios'
import Loading from 'components/Loading'
import { useAuth } from '../../context'
import { useAuthStore } from 'store'
import { useForm } from 'react-hook-form'
import { useObserver } from 'mobx-react-lite'
import { useSnackbar } from '../../lib'

const useStores = () => {
  const authStore = useAuthStore()

  return useObserver(() => ({
    availableUsersLoading: authStore.availableUsers.status !== 'success',
    availableUsers: authStore.availableUsers.data,
    loadUsers: authStore.loadUsers,
  }))
}

interface LoginFormData {
  name: string
  password: string
}

export const Login = () => {
  const { availableUsers, availableUsersLoading, loadUsers } = useStores()
  const { login } = useAuth()
  const { errorSnackbar, successSnackbar } = useSnackbar()

  const { register, reset, formState, handleSubmit, setValue, getValues } = useForm<LoginFormData>({
    defaultValues: { name: '', password: '' },
  })

  useEffect(() => {
    loadUsers()
  }, [loadUsers])

  useEffect(() => {
    document.body.classList.add('login')
    return () => {
      document.body.classList.remove('login')
    }
  }, [])

  useEffect(() => {
    if (availableUsers.length) {
      setValue('name', availableUsers[0].name)
    }
  }, [availableUsers, setValue])

  if (availableUsersLoading) {
    return <Loading />
  }

  const onSubmit = async ({ name, password }: LoginFormData) => {
    try {
      await login(name, password)
      successSnackbar('Erfolgreich angemeldet.')
    } catch (e) {
      reset({ name: getValues('name') })
      if ((e as AxiosError<unknown>).response?.status === 401) {
        errorSnackbar('Benutzername oder Passwort falsch.')
      } else {
        errorSnackbar('Es ist ein allgemeiner Fehler aufgetreten. Bitte später erneut probieren.', {
          autoHideDuration: 30000,
        })
      }
    }
  }

  return (
    <div className="login--container">
      <div className="login--container-image"></div>
      <div className="login--container-form-container">
        <div className="login--form-container">
          <h5>Anmeldung bei Beachist</h5>
          <p className="text-muted">Willkommen zurück. Bitte gib dein Passwort ein, um dich anzumelden.</p>
          <form action="" onSubmit={handleSubmit(onSubmit)}>
            <div className="form-group">
              <label htmlFor="userSelect">Nutzer</label>
              <select {...register('name')} className="form-control" id="userSelect">
                {availableUsers.map(user => (
                  <option key={user.name} value={user.name}>
                    {user.description}
                  </option>
                ))}
              </select>
              <small id="emailHelp" className="form-text text-muted">
                Bitte auswählen.
              </small>
            </div>

            <div className="form-group">
              <label htmlFor="password">Passwort</label>
              <input
                {...register('password')}
                type="password"
                placeholder="Geheim"
                className="form-control"
                id="password"
              />
            </div>
            <div className="login--container--form--button-container">
              <button disabled={formState.isSubmitting || !formState.isDirty} type="submit" className="btn btn-primary">
                Anmelden
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  )
}
