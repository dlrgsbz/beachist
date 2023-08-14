import React, { useEffect } from 'react'
import { StationSelect, allStations } from './StationSelect'

import { BaseStationInfo } from '../../../dtos'
import { Modal } from 'components/Modal'
import { useForm } from 'react-hook-form'
import { useSnackbar } from 'lib'

export interface CreateFieldModalProps {
  onClose: () => void
}

interface FieldFormData {
  name: string
  stations: BaseStationInfo[]
  required?: number
  note?: string
}

export const CreateFieldModal: React.VFC<CreateFieldModalProps> = ({ onClose }) => {
  const { errorSnackbar, successSnackbar } = useSnackbar()

  const { register, reset, formState, handleSubmit, setValue, watch } = useForm<FieldFormData>({
    defaultValues: { name: '', stations: [allStations], required: 1, note: '' },
  })

  useEffect(() => {
    register('stations')
  }, [register])

  const onSubmit = async (data: FieldFormData) => {
    console.log(data)
    successSnackbar('Erfolgreich angelegt')
    onClose()
  }

  return (
    <form action="" onSubmit={handleSubmit(onSubmit)}>
      <Modal
        title={<>Neues Material</>}
        onClose={onClose}
        submitButton={
          <button disabled={formState.isSubmitting || !formState.isDirty} type="submit" className="btn btn-primary">
            Material anlegen
          </button>
        }
      >
        <div className="form-group">
          <label htmlFor="name">Name</label>
          <input
            type="text"
            className="form-control"
            id="name"
            aria-describedby="nameHelp"
            placeholder="Materialname"
            required
            {...register('name')}
          />
          <small id="nameHelp" className="form-text text-muted">
            Ein aussagekräftiger Name, mit dem Rettungsschwimmer*innen etwas anfangen können.
          </small>
        </div>
        <div className="form-group">
          <label htmlFor="station">Station</label>
          <StationSelect onChange={val => setValue('stations', val)} value={watch('stations')} />
          <small id="stationHelp" className="form-text text-muted">
            Entweder einzelne Stationen oder alle auswählen.
          </small>
        </div>
        <div className="form-group">
          <label htmlFor="required">Anzahl</label>
          <input
            type="number"
            className="form-control"
            id="required"
            aria-describedby="requiredHelp"
            placeholder="Anzahl"
            min={0}
            {...register('required')}
          />
          <small id="requiredHelp" className="form-text text-muted">
            Wie viele von diesem Material soll sich auf den Stationen befinden.
          </small>
        </div>
        <div className="form-group">
          <label htmlFor="note">Bemerkung</label>
          <input
            type="text"
            className="form-control"
            id="note"
            aria-describedby="noteHelp"
            placeholder="Bemerkung"
            {...register('note')}
          />
          <small id="noteHelp" className="form-text text-muted">
            Ggf. Anmerkungen für dieses Material.
          </small>
        </div>
      </Modal>
    </form>
  )
}
