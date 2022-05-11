import { ProvisioningModal, ProvisioningModalProps, StationRow } from './components'
import React, { useEffect, useState } from 'react'
import { isSuccessful, useSnackbar } from 'lib'

import { SkeletonRow } from 'components/Skeletons'
import classNames from 'classnames'
import { useAdminStore } from 'store'
import { useObserver } from 'mobx-react-lite'

const useStores = () => {
  const adminStore = useAdminStore()

  return useObserver(() => ({
    loading: adminStore.stationsState.status === 'pending',
    stations: adminStore.stationsState.data,
    provisionMap: adminStore.provisionMapState.data,
    provisionMapLoading: adminStore.provisionMapState.status === 'pending',
    fetchProvisioningRequests: adminStore.fetchProvisioningRequests,
    fetchData: adminStore.fetchData,
    createProvisioning: adminStore.createProvisioningRequest,
  }))
}

export const AdminStation: React.VFC = () => {
  const [modal, setModal] = useState<ProvisioningModalProps | undefined>(undefined)
  const {
    loading,
    provisionMapLoading,
    stations,
    provisionMap,
    fetchData,
    fetchProvisioningRequests,
    createProvisioning,
  } = useStores()
  const { errorSnackbar, successSnackbar } = useSnackbar()

  useEffect(() => {
    fetchData()
  }, [fetchData])

  const onCreateProvisioning = async (stationId: string, stationName: string): Promise<void> => {
    const result = await createProvisioning(stationId)

    if (isSuccessful(result) && result.data) {
      successSnackbar('Zuweisung erfolgreich angelegt.')
      await fetchProvisioningRequests()
      // show provision password for this tablet
      setModal({
        provisioningRequest: result.data,
        stationName,
        onClose: () => setModal(undefined),
      })
      return
    }

    errorSnackbar('Beim Anlegen der Zuweisung ist ein Fehler aufgetreten.')
  }

  const tableClasses = classNames('table', 'table-striped', 'table-no-bordered')

  return (
    <div>
      <h1>Stationsverwaltung</h1>

      {modal && <ProvisioningModal {...modal} />}

      <table className={tableClasses}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Online-Status</th>
            <th>App-Version</th>
            <th>Zuweisung</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <SkeletonRow rows={3} columns={4} />
          ) : (
            stations.map(station => (
              <StationRow
                key={station.id}
                station={station}
                provisionLoading={provisionMapLoading}
                provision={provisionMap[station.id]}
                createProvisioning={onCreateProvisioning}
                setModal={setModal}
              />
            ))
          )}
        </tbody>
      </table>
    </div>
  )
}
