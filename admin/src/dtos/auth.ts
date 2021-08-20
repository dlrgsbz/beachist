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
  permissions: Permission[]
}

export interface UserInfoWithToken extends UserInfo {
  token: string
}