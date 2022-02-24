import React, { ReactElement, useEffect } from 'react'
import { Portal } from '@mui/material'

interface ModalProps {
  title: ReactElement
  primaryTitle?: string
  primaryClick?: () => void
  onClose: () => void
}

export const Modal: React.FC<ModalProps> = (
  {
    title,
    primaryTitle,
    primaryClick,
    onClose,
    children,
  },
) => {
  useEffect(() => {
    document.body.classList.add('modal-open')

    return () => {
      document.body.classList.remove('modal-open')
    }
  })

  const style = {
    display: 'block',
  }

  return <Portal>
    <div className="modal-backdrop fade show"/>
    <div className="modal fade show" id="exampleModalCenter" tabIndex={-1} role="dialog"
         aria-labelledby="exampleModalCenterTitle" aria-hidden="false" style={style}>
      <div className="modal-dialog modal-dialog-centered" role="document">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title" id="exampleModalLongTitle">{title}</h5>
            <button type="button" className="close" data-dismiss="modal" aria-label="Close" onClick={onClose}>
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div className="modal-body">
            {children}
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Schließen</button>
            {primaryTitle &&
            <button type="button" onClick={primaryClick} className="btn btn-primary">{primaryTitle}</button>}
          </div>
        </div>
      </div>
    </div>
  </Portal>
}