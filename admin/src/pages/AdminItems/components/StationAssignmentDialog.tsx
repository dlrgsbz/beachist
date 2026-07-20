import { Field, StationInfo } from 'dtos'
import React, { useMemo, useState } from 'react'

interface StationAssignmentDialogProps {
  item: Field
  stations: StationInfo[]
  initialGlobal: boolean
  initialStationIds: string[]
  initialRequired?: number | null
  initialNote?: string | null
  saving?: boolean
  onSave: (global: boolean, stationIds: string[], required: number | null, note: string | null) => void
  onClose: () => void
}

export const StationAssignmentDialog: React.FC<StationAssignmentDialogProps> = ({
  item,
  stations,
  initialGlobal,
  initialStationIds,
  initialRequired,
  initialNote,
  saving,
  onSave,
  onClose,
}) => {
  const [global, setGlobal] = useState(initialGlobal)
  const [selected, setSelected] = useState<Set<string>>(new Set(initialStationIds))
  const [search, setSearch] = useState('')
  const [required, setRequired] = useState(
    initialRequired === null || initialRequired === undefined ? '' : String(initialRequired),
  )
  const [note, setNote] = useState(initialNote ?? '')

  const filteredStations = useMemo(() => {
    const term = search.trim().toLowerCase()
    if (term === '') {
      return stations
    }
    return stations.filter(station => station.name.toLowerCase().includes(term))
  }, [stations, search])

  const toggleStation = (stationId: string) => {
    setSelected(prev => {
      const next = new Set(prev)
      if (next.has(stationId)) {
        next.delete(stationId)
      } else {
        next.add(stationId)
      }
      return next
    })
  }

  const selectAll = () => setSelected(new Set(stations.map(station => station.id)))
  const deselectAll = () => setSelected(new Set())

  const handleSave = () => {
    const trimmedRequired = required.trim()
    const parsedRequired = trimmedRequired === '' ? null : Number.parseInt(trimmedRequired, 10)
    const normalizedRequired = parsedRequired === null || Number.isNaN(parsedRequired) ? null : parsedRequired
    const trimmedNote = note.trim()
    onSave(global, Array.from(selected), normalizedRequired, trimmedNote === '' ? null : trimmedNote)
  }

  return (
    <div
      role="dialog"
      aria-modal="true"
      onClick={onClose}
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1050,
      }}
    >
      <div
        className="card"
        onClick={event => event.stopPropagation()}
        style={{ width: 'min(560px, 92vw)', maxHeight: '86vh', display: 'flex', flexDirection: 'column' }}
      >
        <div className="card-header d-flex align-items-center justify-content-between">
          <strong>Zuweisung: {item.name}</strong>
          <button type="button" className="close" aria-label="Schließen" onClick={onClose}>
            <span aria-hidden="true">&times;</span>
          </button>
        </div>

        <div className="card-body" style={{ overflowY: 'auto' }}>
          <div className="custom-control custom-switch mb-3">
            <input
              type="checkbox"
              className="custom-control-input"
              id={`global-${item.id}`}
              checked={global}
              onChange={event => setGlobal(event.target.checked)}
            />
            <label className="custom-control-label" htmlFor={`global-${item.id}`}>
              Für alle Stationen (global)
            </label>
          </div>

          {global ? (
            <p className="text-muted mb-0">
              Dieser Eintrag ist allen Stationen zugewiesen. Einzelne Stationen können ausgewählt werden, sobald
              „global“ deaktiviert ist.
            </p>
          ) : (
            <>
              <div className="d-flex align-items-center mb-2">
                <input
                  type="text"
                  className="form-control form-control-sm mr-2"
                  placeholder="Station suchen…"
                  value={search}
                  onChange={event => setSearch(event.target.value)}
                />
                <button type="button" className="btn btn-sm btn-outline-secondary mr-2" onClick={selectAll}>
                  Alle
                </button>
                <button type="button" className="btn btn-sm btn-outline-secondary" onClick={deselectAll}>
                  Keine
                </button>
              </div>

              <div className="text-muted small mb-2">
                {selected.size} von {stations.length} Stationen ausgewählt
              </div>

              <div style={{ maxHeight: '40vh', overflowY: 'auto' }}>
                {filteredStations.map(station => (
                  <div className="custom-control custom-checkbox mb-1" key={station.id}>
                    <input
                      type="checkbox"
                      className="custom-control-input"
                      id={`station-${item.id}-${station.id}`}
                      checked={selected.has(station.id)}
                      onChange={() => toggleStation(station.id)}
                    />
                    <label className="custom-control-label" htmlFor={`station-${item.id}-${station.id}`}>
                      {station.name}
                    </label>
                  </div>
                ))}
                {filteredStations.length === 0 && <p className="text-muted mb-0">Keine Station gefunden.</p>}
              </div>
            </>
          )}

          <hr />

          <div className="form-group mb-2">
            <label htmlFor={`required-${item.id}`}>Benötigte Anzahl (optional)</label>
            <input
              type="number"
              min={0}
              className="form-control"
              id={`required-${item.id}`}
              placeholder="z. B. 2"
              value={required}
              onChange={event => setRequired(event.target.value)}
            />
            <small className="form-text text-muted">Wird in der App als Sollmenge angezeigt.</small>
          </div>

          <div className="form-group mb-0">
            <label htmlFor={`note-${item.id}`}>Notiz (optional)</label>
            <textarea
              className="form-control"
              id={`note-${item.id}`}
              rows={2}
              placeholder="Hinweis, der in der App angezeigt wird"
              value={note}
              onChange={event => setNote(event.target.value)}
            />
          </div>
        </div>

        <div className="card-footer d-flex justify-content-end">
          <button type="button" className="btn btn-outline-secondary mr-2" onClick={onClose} disabled={saving}>
            Abbrechen
          </button>
          <button type="button" className="btn btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Speichern…' : 'Speichern'}
          </button>
        </div>
      </div>
    </div>
  )
}
