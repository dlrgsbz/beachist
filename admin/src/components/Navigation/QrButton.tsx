import React, { useEffect, useState } from 'react'

import Loading from '../Loading'
import { Modal } from '../Modal'
import { Permission } from 'dtos'
import { QRCodeCanvas as QRCode } from 'qrcode.react'
import { Restricted } from '../auth/Restricted'
import { useAuthStore } from 'store'
import { useObserver } from 'mobx-react-lite'

const useStores = () => {
  const authStore = useAuthStore()

  return useObserver(() => ({
    loginTokenLoading: authStore.loginToken.status !== 'success',
    loginToken: authStore.loginToken.data,
    loadLoginToken: authStore.loadLoginToken,
  }))
}

export const QrButton: React.FC = () => {
  const [open, setOpen] = useState(false)

  const { loadLoginToken, loginToken, loginTokenLoading } = useStores()

  useEffect(() => {
    if (open) {
      loadLoginToken()
    }
  }, [loadLoginToken, open])

  if (open && loginTokenLoading) {
    return <Loading />
  }

  const url = `${window.location.protocol}//${window.location.host}/login#token=${loginToken}`

  return (
    <Restricted permission={Permission.qr}>
      <>
        {open && (
          <Modal title={<>Login-Code</>} onClose={() => setOpen(false)}>
            <QRCode value={url} size={512} />
          </Modal>
        )}
        <li className="nav-item">
          <button
            className="btn btn-outline-primary btn-sm nav-link d-inline mx-2 p-2"
            type="button"
            onClick={() => setOpen(!open)}
          >
            Login-QR-Code erstellen
          </button>
        </li>
      </>
    </Restricted>
  )
}
