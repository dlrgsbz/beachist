import { ProvisioningRequest } from 'dtos'
import React from 'react'
import Skeleton from 'react-loading-skeleton'

interface ProvisioningInfoProps {
  createProvisioning: () => Promise<void>
  loading: boolean
  provisioning?: ProvisioningRequest
  setModal: () => void
}

export const ProvisioningInfo: React.VFC<ProvisioningInfoProps> = ({
  createProvisioning,
  setModal,
  loading,
  provisioning,
}) => {
  const onCreateProvisioning = () => createProvisioning()

  if (loading) {
    return <Skeleton />
  }

  if (!provisioning) {
    return <button className='btn btn-link' onClick={onCreateProvisioning}>Neue Zuweisung</button>
  }

  const expiresAt = provisioning.expiresAt.fromNow(false)
  const expiresAtAbsolute = provisioning.expiresAt.format('L LTS')
  return <>
    <button className='btn btn-link' onClick={() => setModal()}>Passwort anzeigen</button>
    <br />
    läuft <abbr title={expiresAtAbsolute}>{expiresAt}</abbr> ab
  </>
}
