import { ProvisioningRequest, StationInfo } from 'dtos'
import React, { useCallback } from 'react'

import { ProvisioningInfo } from './ProvisioningInfo'
import { ProvisioningModalProps } from './ProvisioningModal'

interface StationRowProps {
  station: StationInfo
  provisionLoading: boolean
  provision?: ProvisioningRequest
  createProvisioning: (stationId: string, stationName: string) => Promise<void>
  setModal: (data: ProvisioningModalProps | undefined) => void
}

export const StationRow: React.VFC<StationRowProps> = ({
  station,
  provisionLoading,
  provision,
  createProvisioning,
  setModal,
}) => {
  const { name, online, onlineStateSince, appVersion, appVersionCode, id } = station
  const onlineString = online ? 'online' : 'offline'
  const onlineStateSinceString = onlineStateSince ? `seit ${onlineStateSince.format('L LTS')}` : 'noch nie'

  const onSetModal = useCallback(() => {
    if (provision) {
      setModal({
        provisioningRequest: provision,
        stationName: station.name,
        onClose: () => setModal(undefined),
      })
    }
  }, [provision, setModal, station.name])

  return (
    <tr>
      <td>{name}</td>
      <td>
        {onlineString} ({onlineStateSinceString})
      </td>
      <td>{appVersion ? `${appVersion} (${appVersionCode})` : 'unbekannt'}</td>
      <td>
        <ProvisioningInfo
          loading={provisionLoading}
          provisioning={provision}
          createProvisioning={() => createProvisioning(id, name)}
          setModal={onSetModal}
        />
      </td>
    </tr>
  )
}
