export interface User {
  name: string
}

export interface UserInfo extends User {
  description: string
}

export interface UserInfoWithToken extends UserInfo {
  token: string
}