export enum Permission {
  user = 'ROLE_USER',
  admin = 'ROLE_ADMIN',
  qr = 'ROLE_QR',
}

export interface User {
  name: string
}

export interface UserInfo extends User {
  description: string
  roles: Permission[]
  id: string
}

export interface UserInfoWithToken extends UserInfo {
  access: string
}