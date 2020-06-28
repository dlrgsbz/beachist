import React from 'react'
import './loading.scss'

const Loading: React.FC = () => {
  return (
    <div className="loading">
      <div className="loading__child loading__child--bounce1" />
      <div className="loading__child loading__child--bounce2" />
      <div className="loading__child loading__child--bounce3" />
    </div>
  )
}

export default Loading
