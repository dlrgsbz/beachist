import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'
import React from 'react'

interface ButtonProps {
  onClick?: () => void
}

export const EditButton: React.FC<ButtonProps> = ({ children, onClick }) => (
  <button type="button" onClick={onClick} className="btn btn-primary">
    <EditIcon />
    {children}
  </button>
)

export const DeleteButton: React.FC<ButtonProps> = ({ children, onClick }) => (
  <button type="button" onClick={onClick} className="btn btn-danger">
    <DeleteIcon />
    {children}
  </button>
)

export const AddButton: React.FC<ButtonProps> = ({ children, onClick }) => (
  <button type="button" onClick={onClick} className="btn btn-primary">
    <AddIcon />
    {children}
  </button>
)
