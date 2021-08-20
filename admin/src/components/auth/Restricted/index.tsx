import { Permission, UserInfo } from 'dtos'
import React, { ReactElement } from 'react'
import { useAuth } from 'context'

interface RestrictedProps {
  permission: Permission
  fallback?: ReactElement
}

export const Restricted: React.FC<RestrictedProps> = ({ permission, fallback, children }) => {
  const { user } = useAuth()

  if (authorizedFor(user, permission)) {
    return <>{children}</>
  }

  return <>{fallback}</> || null
}

const authorizedFor = (user: UserInfo | null, permission: Permission): boolean => {
  return !!user && user.permissions.includes(permission)
}