import { AssignmentList, ItemForm, ItemFormValues, SortableItems, StationAssignmentDialog } from './components'
import { Link, Route, Routes, useLocation } from 'react-router-dom'
import React, { useEffect, useState } from 'react'
import { isSuccessful, useSnackbar } from 'lib'

import { Field } from 'dtos'
import { ItemPayload } from 'services'
import { SkeletonRow } from 'components/Skeletons'
import classNames from 'classnames'
import { useItemsStore } from 'store'
import { useObserver } from 'mobx-react-lite'

const useStores = () => {
  const itemsStore = useItemsStore()

  return useObserver(() => {
    const assignments = itemsStore.assignmentsState.data

    return {
      loading:
        itemsStore.itemsState.status === 'pending' ||
        itemsStore.stationsState.status === 'pending' ||
        itemsStore.assignmentsState.status === 'pending',
      mutating: itemsStore.mutationState.status === 'pending',
      items: itemsStore.itemsState.data,
      stations: itemsStore.stationsState.data,
      fetchAll: itemsStore.fetchAll,
      createItem: itemsStore.createItem,
      updateItem: itemsStore.updateItem,
      deleteItem: itemsStore.deleteItem,
      reorderItems: itemsStore.reorderItems,
      saveAssignments: itemsStore.saveAssignments,
      isGlobal: (fieldId: string): boolean => assignments.some(a => a.id === fieldId && a.station === null),
      assignedStationIds: (fieldId: string): string[] =>
        assignments.filter(a => a.id === fieldId && a.station !== null).map(a => a.station as string),
      assignmentRequired: (fieldId: string): number | null => {
        const assignment = assignments.find(a => a.id === fieldId)
        return assignment?.required ?? null
      },
      assignmentNote: (fieldId: string): string | null => {
        const assignment = assignments.find(a => a.id === fieldId)
        return assignment?.note ?? null
      },
    }
  })
}

const useCurrentTab = () => {
  const { pathname } = useLocation()

  if (pathname === '/admin/items') {
    return 'items'
  } else {
    return 'assignments'
  }
}

export const AdminItems: React.FC = () => {
  const {
    loading,
    mutating,
    items,
    stations,
    fetchAll,
    createItem,
    updateItem,
    deleteItem,
    reorderItems,
    saveAssignments,
    isGlobal,
    assignedStationIds,
    assignmentRequired,
    assignmentNote,
  } = useStores()
  const { errorSnackbar, successSnackbar } = useSnackbar()
  const [editingId, setEditingId] = useState<string | undefined>(undefined)
  const [dialogItem, setDialogItem] = useState<Field | undefined>(undefined)

  useEffect(() => {
    fetchAll()
  }, [fetchAll])

  const onCreate = async (values: ItemFormValues): Promise<void> => {
    const payload: ItemPayload = {
      name: values.name.trim(),
      parent: values.parent === '' ? null : values.parent,
    }
    const result = await createItem(payload)
    if (isSuccessful(result)) {
      successSnackbar('Item angelegt.')
    } else {
      errorSnackbar('Beim Anlegen des Items ist ein Fehler aufgetreten.')
    }
  }

  const onUpdate = async (id: string, values: ItemFormValues): Promise<void> => {
    const current = items.find(item => item.id === id)
    const payload: ItemPayload = {
      name: values.name.trim(),
      parent: values.parent === '' ? null : values.parent,
      sortId: current?.sortId ?? null,
    }
    const result = await updateItem(id, payload)
    if (isSuccessful(result)) {
      successSnackbar('Item gespeichert.')
      setEditingId(undefined)
    } else {
      errorSnackbar('Beim Speichern des Items ist ein Fehler aufgetreten.')
    }
  }

  const onDelete = async (item: Field): Promise<void> => {
    if (!window.confirm(`Item "${item.name}" wirklich entfernen?`)) {
      return
    }
    const result = await deleteItem(item.id)
    if (isSuccessful(result)) {
      successSnackbar('Item entfernt.')
    } else {
      errorSnackbar('Beim Entfernen des Items ist ein Fehler aufgetreten.')
    }
  }

  const onReorder = async (orderedIds: string[]): Promise<void> => {
    const result = await reorderItems(orderedIds)
    if (!isSuccessful(result)) {
      errorSnackbar('Die Reihenfolge konnte nicht gespeichert werden.')
    }
  }

  const onSaveAssignments = async (
    global: boolean,
    stationIds: string[],
    required: number | null,
    note: string | null,
  ): Promise<void> => {
    if (!dialogItem) {
      return
    }
    const result = await saveAssignments(dialogItem.id, global, stationIds, required, note)
    if (isSuccessful(result)) {
      successSnackbar('Zuweisung gespeichert.')
      setDialogItem(undefined)
    } else {
      errorSnackbar('Die Zuweisung konnte nicht geändert werden.')
    }
  }

  const selectedTab = useCurrentTab()

  return (
    <div>
      <h1>Checkliste verwalten</h1>

      <div className="card mt-3">
        <div className="card-header">
          <ul className="nav nav-tabs card-header-tabs">
            <li className="nav-item">
              <Link className={classNames({ 'nav-link': true, active: selectedTab === 'items' })} to="/admin/items">
                Checklist-Einträge
              </Link>
            </li>
            <li className="nav-item">
              <Link
                className={classNames({ 'nav-link': true, active: selectedTab === 'assignments' })}
                to="/admin/items/assignments"
              >
                Stationszuweisung
              </Link>
            </li>
          </ul>
        </div>

        <div className="card-body">
          <Routes>
            <Route
              path="/"
              element={
                <>
                  <section className="mb-4">
                    <h2 className="h4">Neuer Eintrag</h2>
                    <ItemForm items={items} submitLabel="Hinzufügen" disabled={mutating} onSubmit={onCreate} />
                  </section>

                  <section className="mb-4">
                    <h2 className="h4">Einträge</h2>
                    <p className="text-muted">
                      Einträge können sortiert werden und werden entsprechend auf den Stationstablets angezeigt. Ziehe
                      die Einträge per Drag & Drop an die gewünschte Position.
                    </p>
                    {loading ? (
                      <table className="table">
                        <tbody>
                          <SkeletonRow rows={3} columns={4} />
                        </tbody>
                      </table>
                    ) : (
                      <SortableItems
                        items={items}
                        editingId={editingId}
                        disabled={mutating}
                        onEdit={setEditingId}
                        onCancelEdit={() => setEditingId(undefined)}
                        onUpdate={onUpdate}
                        onDelete={onDelete}
                        onReorder={onReorder}
                      />
                    )}
                  </section>
                </>
              }
            />

            <Route
              path="/assignments"
              element={
                <>
                  <section className="mb-4">
                    <h2 className="h4">Zuweisung zu Stationen</h2>
                    <p className="text-muted">
                      Einträge können entweder mehreren Stationen im einzelnen oder aber allen Stationen zugewiesen
                      werden. Bei der Zuweisung kann jeweils eine Mindestanzahl sowie eine Notiz hinterlegt werden, die
                      auf den Stationstablets angezeigt wird.
                    </p>
                    {loading ? (
                      <table className="table">
                        <tbody>
                          <SkeletonRow rows={3} columns={4} />
                        </tbody>
                      </table>
                    ) : (
                      <AssignmentList
                        items={items}
                        stations={stations}
                        isGlobal={isGlobal}
                        assignedStationIds={assignedStationIds}
                        assignmentRequired={assignmentRequired}
                        assignmentNote={assignmentNote}
                        disabled={mutating}
                        onEdit={setDialogItem}
                      />
                    )}
                  </section>
                </>
              }
            />
          </Routes>
          {dialogItem && (
            <StationAssignmentDialog
              item={dialogItem}
              stations={stations}
              initialGlobal={isGlobal(dialogItem.id)}
              initialStationIds={assignedStationIds(dialogItem.id)}
              initialRequired={assignmentRequired(dialogItem.id)}
              initialNote={assignmentNote(dialogItem.id)}
              saving={mutating}
              onSave={onSaveAssignments}
              onClose={() => setDialogItem(undefined)}
            />
          )}
        </div>
      </div>
    </div>
  )
}
