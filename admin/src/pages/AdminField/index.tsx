import './AdminField.scss'

import * as React from 'react'

import { CreateFieldModal, CreateFieldModalProps, FieldRow } from './components'
import { useEffect, useState } from 'react'

import { AddButton } from 'components/buttons'
import { SkeletonRow } from 'components/Skeletons'
import classNames from 'classnames'
import { useAdminFieldStore } from '../../store'
import { useObserver } from 'mobx-react-lite'

const FeldRow: React.FC<{ uuid: string }> = ({ uuid }) => {
  // const { field, onFeldChange, setFeldRequired, onCommentChange } = {...}

  return <tr key={uuid} />

  // return <tr>
  //   <td><input type="text" value={field.name} onChange={e => onFeldChange(e.target.value)}/></td>
  //   <td>
  //     <input type="checkbox" checked={(typeof field.required === 'undefined')}
  //            onChange={event => setFeldRequired((typeof field.required === 'undefined') ? 1 : undefined)}/>&nbsp; Nur
  //     Überschrift <br/>
  //     <input type="number" disabled={(typeof field.required === 'undefined')}
  //            onChange={event => setFeldRequired(parseInt(event.target.value, 10))} value={field.required}/>
  //   </td>
  //   <td><input type="text" value={field.note} onChange={event => onCommentChange(event.target.value)}/></td>
  //   <td>
  //     <input type='checkbox' id='alle'/><label htmlFor='alle'>alle Stationen</label>
  //     <select multiple={true}>
  //       {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 'Offendorf'].map(station =>
  //         <option>Station {station}</option>)}
  //     </select>
  //   </td>
  // </tr>
}

const useStores = () => {
  const adminFieldStore = useAdminFieldStore()

  return useObserver(() => ({
    loading: adminFieldStore.fieldsState.status === 'pending',
    fields: adminFieldStore.fieldsState.data,
    fetchFields: adminFieldStore.fetchFields,
  }))
}

export const AdminField: React.VFC = () => {
  const [modal, setModal] = useState<CreateFieldModalProps | undefined>(undefined)
  const { loading, fields, fetchFields } = useStores()
  const tableClasses = classNames('table', 'table-striped', 'table-no-bordered')

  useEffect(() => {
    fetchFields()
  }, [fetchFields])

  const createModal = () => {
    const modalProps: CreateFieldModalProps = {
      onClose: () => setModal(undefined),
    }
    setModal(modalProps)
  }

  return (
    <div>
      <h1>Materialverwaltung</h1>

      {modal && <CreateFieldModal {...modal} />}

      <div className="button-container">
        <AddButton onClick={createModal}>Neues Material</AddButton>
      </div>

      <table className={tableClasses}>
        <thead>
          <tr>
            <th>Name</th>
            <th>Stationen</th>
            <th>Anzahl</th>
            <th>Bemerkung</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          {loading ? <SkeletonRow rows={3} columns={3} /> : fields.map(item => <FieldRow key={item.id} field={item} />)}
        </tbody>
      </table>
    </div>
  )
}
