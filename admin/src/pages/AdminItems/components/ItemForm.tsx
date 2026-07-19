import React, { useMemo, useState } from 'react'

import { Field } from 'dtos'

export interface ItemFormValues {
  name: string
  parent: string
}

interface ItemFormProps {
  items: Field[]
  initialValues?: ItemFormValues
  submitLabel: string
  disabled?: boolean
  excludeId?: string
  onSubmit: (values: ItemFormValues) => void
  onCancel?: () => void
}

export const emptyItemForm: ItemFormValues = { name: '', parent: '' }

export const ItemForm: React.FC<ItemFormProps> = ({
  items,
  initialValues = emptyItemForm,
  submitLabel,
  disabled,
  excludeId,
  onSubmit,
  onCancel,
}) => {
  const [values, setValues] = useState<ItemFormValues>(initialValues)

  const parentOptions = useMemo(() => items.filter(item => item.id !== excludeId), [items, excludeId])

  const canSubmit = values.name.trim().length > 0 && !disabled

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    if (!canSubmit) {
      return
    }
    onSubmit(values)
    if (!onCancel) {
      setValues(emptyItemForm)
    }
  }

  return (
    <form className="form-inline" onSubmit={handleSubmit}>
      <input
        type="text"
        className="form-control mr-2 mb-2"
        placeholder="Name"
        value={values.name}
        onChange={e => setValues({ ...values, name: e.target.value })}
      />
      <select
        className="form-control mr-2 mb-2"
        value={values.parent}
        onChange={e => setValues({ ...values, parent: e.target.value })}
      >
        <option value="">Keine Überschrift</option>
        {parentOptions.map(item => (
          <option key={item.id} value={item.id}>
            {item.name}
          </option>
        ))}
      </select>
      <button type="submit" className="btn btn-primary mr-2 mb-2" disabled={!canSubmit}>
        {submitLabel}
      </button>
      {onCancel && (
        <button type="button" className="btn btn-outline-secondary mb-2" onClick={onCancel}>
          Abbrechen
        </button>
      )}
    </form>
  )
}
