import './SortableItem.scss'

import { ItemForm, ItemFormValues } from './ItemForm'
import React, { useMemo, useRef, useState } from 'react'

import { Field } from 'dtos'
import classNames from 'classnames'

interface TreeNode {
  field: Field
  children: TreeNode[]
}

const buildTree = (items: Field[]): TreeNode[] => {
  const byId = new Map<string, TreeNode>(items.map(item => [item.id, { field: item, children: [] }]))
  const roots: TreeNode[] = []
  items.forEach(item => {
    const node = byId.get(item.id)!
    if (item.parent && byId.has(item.parent)) {
      byId.get(item.parent)!.children.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

const flatten = (
  nodes: TreeNode[],
  overrideParentId: string | null,
  overrideIds: string[],
  parentId: string | null = null,
): string[] => {
  const ordered =
    parentId === overrideParentId
      ? overrideIds
          .map(id => nodes.find(node => node.field.id === id))
          .filter((node): node is TreeNode => node !== undefined)
      : nodes

  const result: string[] = []
  ordered.forEach(node => {
    result.push(node.field.id)
    result.push(...flatten(node.children, overrideParentId, overrideIds, node.field.id))
  })
  return result
}

const move = (ids: string[], draggingId: string, overId: string): string[] => {
  const from = ids.indexOf(draggingId)
  const to = ids.indexOf(overId)
  if (from === -1 || to === -1) {
    return ids
  }
  const next = [...ids]
  next.splice(from, 1)
  next.splice(to, 0, draggingId)
  return next
}

const sameOrder = (a: string[], b: string[]): boolean =>
  a.length === b.length && a.every((value, index) => value === b[index])

interface SortableListProps {
  ids: string[]
  disabled?: boolean
  onReorder: (orderedIds: string[]) => void
  renderRow: (id: string) => React.ReactNode
}

const SortableList: React.FC<SortableListProps> = ({ ids, disabled, onReorder, renderRow }) => {
  const [order, setOrder] = useState<string[]>(ids)
  const [draggingId, setDraggingId] = useState<string | null>(null)
  const orderRef = useRef<string[]>(order)
  orderRef.current = order

  if (!draggingId && !sameOrder(order, ids)) {
    setOrder(ids)
  }

  const handleDragOver = (event: React.DragEvent, overId: string) => {
    if (!draggingId) {
      return
    }
    event.preventDefault()
    if (draggingId === overId) {
      return
    }
    setOrder(prev => move(prev, draggingId, overId))
  }

  const handleDragEnd = () => {
    const next = orderRef.current
    setDraggingId(null)
    if (!sameOrder(next, ids)) {
      onReorder(next)
    }
  }

  return (
    <ul className="list-group">
      {order.map(id => (
        <li
          key={id}
          className={classNames('list-group-item', 'd-flex', 'align-items-center', 'sortable-item', {
            'sortable-item--dragging': draggingId === id,
          })}
          onDragOver={event => handleDragOver(event, id)}
          onDrop={event => event.preventDefault()}
          style={{ opacity: draggingId === id ? 0.4 : 1 }}
        >
          {!disabled && (
            <span
              draggable
              onDragStart={() => setDraggingId(id)}
              onDragEnd={handleDragEnd}
              title="Ziehen zum Sortieren"
              aria-label="Ziehen zum Sortieren"
              className="dragging-indicator"
            >
              ⣿
            </span>
          )}
          <div className="flex-grow-1">{renderRow(id)}</div>
        </li>
      ))}
    </ul>
  )
}

interface TreeLevelProps {
  nodes: TreeNode[]
  parentId: string | null
  items: Field[]
  editingId?: string
  disabled?: boolean
  onReorderGroup: (parentId: string | null, orderedIds: string[]) => void
  onEdit: (id: string) => void
  onCancelEdit: () => void
  onUpdate: (id: string, values: ItemFormValues) => void
  onDelete: (item: Field) => void
}

const TreeLevel: React.FC<TreeLevelProps> = ({
  nodes,
  parentId,
  items,
  editingId,
  disabled,
  onReorderGroup,
  onEdit,
  onCancelEdit,
  onUpdate,
  onDelete,
}) => (
  <SortableList
    ids={nodes.map(node => node.field.id)}
    disabled={disabled || editingId !== undefined}
    onReorder={orderedIds => onReorderGroup(parentId, orderedIds)}
    renderRow={id => {
      const node = nodes.find(candidate => candidate.field.id === id)
      if (!node) {
        return null
      }
      const { field, children } = node
      const isHeading = children.length > 0

      return (
        <div>
          {editingId === field.id ? (
            <ItemForm
              items={items}
              excludeId={field.id}
              submitLabel="Speichern"
              disabled={disabled}
              initialValues={{ name: field.name, parent: field.parent ?? '' }}
              onSubmit={values => onUpdate(field.id, values)}
              onCancel={onCancelEdit}
            />
          ) : (
            <div className="d-flex align-items-center justify-content-between">
              <span style={{ fontWeight: isHeading ? 600 : 400 }}>{field.name}</span>
              <span>
                <button
                  type="button"
                  className="btn btn-sm btn-outline-primary mr-2"
                  disabled={disabled}
                  onClick={() => onEdit(field.id)}
                >
                  Bearbeiten
                </button>
                <button
                  type="button"
                  className="btn btn-sm btn-outline-danger"
                  disabled={disabled}
                  onClick={() => onDelete(field)}
                >
                  Löschen
                </button>
              </span>
            </div>
          )}

          {isHeading && (
            <div className="mt-2 ml-4">
              <TreeLevel
                nodes={children}
                parentId={field.id}
                items={items}
                editingId={editingId}
                disabled={disabled}
                onReorderGroup={onReorderGroup}
                onEdit={onEdit}
                onCancelEdit={onCancelEdit}
                onUpdate={onUpdate}
                onDelete={onDelete}
              />
            </div>
          )}
        </div>
      )
    }}
  />
)

interface SortableItemsProps {
  items: Field[]
  editingId?: string
  disabled?: boolean
  onEdit: (id: string) => void
  onCancelEdit: () => void
  onUpdate: (id: string, values: ItemFormValues) => void
  onDelete: (item: Field) => void
  onReorder: (orderedIds: string[]) => void
}

export const SortableItems: React.FC<SortableItemsProps> = ({
  items,
  editingId,
  disabled,
  onEdit,
  onCancelEdit,
  onUpdate,
  onDelete,
  onReorder,
}) => {
  const roots = useMemo(() => buildTree(items), [items])

  if (items.length === 0) {
    return <p className="text-muted">Noch keine Items vorhanden.</p>
  }

  const handleReorderGroup = (parentId: string | null, orderedIds: string[]) => {
    onReorder(flatten(roots, parentId, orderedIds))
  }

  return (
    <TreeLevel
      nodes={roots}
      parentId={null}
      items={items}
      editingId={editingId}
      disabled={disabled}
      onReorderGroup={handleReorderGroup}
      onEdit={onEdit}
      onCancelEdit={onCancelEdit}
      onUpdate={onUpdate}
      onDelete={onDelete}
    />
  )
}
