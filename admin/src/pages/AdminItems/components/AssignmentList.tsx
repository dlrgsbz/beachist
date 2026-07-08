import { Field, StationInfo } from 'dtos'

import React, { useMemo } from 'react'

interface AssignmentListProps {
  items: Field[]
  stations: StationInfo[]
  isGlobal: (fieldId: string) => boolean
  assignedStationIds: (fieldId: string) => string[]
  assignmentRequired: (fieldId: string) => number | null
  assignmentNote: (fieldId: string) => string | null
  disabled?: boolean
  onEdit: (item: Field) => void
}

export const AssignmentList: React.VFC<AssignmentListProps> = ({
  items,
  stations,
  isGlobal,
  assignedStationIds,
  assignmentRequired,
  assignmentNote,
  disabled,
  onEdit,
}) => {
  const stationNames = useMemo(() => new Map(stations.map(station => [station.id, station.name])), [stations])

  const summary = (fieldId: string): string => {
    if (isGlobal(fieldId)) {
      return 'Alle Stationen'
    }
    const ids = assignedStationIds(fieldId)
    if (ids.length === 0) {
      return 'Keine Station'
    }
    const names = ids.map(id => stationNames.get(id) ?? '—')
    if (names.length > 3) {
      return `${names.slice(0, 3).join(', ')} +${names.length - 3} weitere`
    }
    return names.join(', ')
  }

  if (items.length === 0) {
    return <p className="text-muted">Noch keine Einträge vorhanden.</p>
  }

  return (
    <table className="table table-striped">
      <thead>
        <tr>
          <th>Eintrag</th>
          <th>Zugewiesen an</th>
          <th style={{ width: 110 }}>Anzahl</th>
          <th>Notiz</th>
          <th style={{ width: 140 }}>Aktion</th>
        </tr>
      </thead>
      <tbody>
        {items.map(item => (
          <tr key={item.id}>
            <td>{item.name}</td>
            <td>
              {isGlobal(item.id) ? (
                <span className="badge badge-success">Alle Stationen</span>
              ) : (
                summary(item.id)
              )}
            </td>
            <td>{assignmentRequired(item.id) ?? <span className="text-muted">–</span>}</td>
            <td>
              {assignmentNote(item.id) ? (
                <span className="text-muted small">{assignmentNote(item.id)}</span>
              ) : (
                <span className="text-muted">–</span>
              )}
            </td>
            <td>
              <button
                type="button"
                className="btn btn-sm btn-outline-primary"
                disabled={disabled}
                onClick={() => onEdit(item)}
              >
                Zuweisen
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
