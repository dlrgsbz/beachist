import * as React from 'react'

import { DeleteButton, EditButton } from 'components/buttons'

import { Field } from 'dtos'

interface FieldRowProps {
  field: Field
}

export const FieldRow: React.VFC<FieldRowProps> = ({ field }) => {
  return (
    <tr>
      <td>{field.name}</td>
      <td>alle</td>
      <td>{field.required || 'kartoffel'}</td>
      <td>{field.note ?? ''}</td>
      <td className="table-button-container">
        <EditButton>Bearbeiten</EditButton>
        <DeleteButton>Löschen</DeleteButton>
      </td>
    </tr>
  )
}
