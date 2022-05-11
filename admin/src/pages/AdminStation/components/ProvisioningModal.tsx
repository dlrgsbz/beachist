import React from 'react'
import { ProvisioningRequest } from 'dtos'
import { Modal } from 'components/Modal'

import './ProvisioningModal.scss'

export interface ProvisioningModalProps {
  stationName: string
  provisioningRequest: ProvisioningRequest
  onClose: () => void
}

export const ProvisioningModal: React.VFC<ProvisioningModalProps> = ({ stationName, provisioningRequest, onClose }) => {
  const { expiresAt, password } = provisioningRequest
  return <Modal title={<>Zuweisung {stationName}</>} onClose={() => onClose()}>
    <div className='d-flex justify-content-center mb-3'>
        <span className='font-weight-bold modal-password font-larger'>
          {password}
        </span>
    </div>

    Läuft ab: {expiresAt.format('L LTS')}
  </Modal>
}
