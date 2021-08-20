import { action, observable } from 'mobx'
import { AsyncState, createAsyncState, runWithAsyncState } from 'lib'
import { User, UserInfo } from 'dtos'
import { ApiClient, fetchAvailableUsers } from 'modules/data'

export class AuthStore {
  @observable availableUsers: AsyncState<UserInfo[]> = createAsyncState([])
  @observable currentUser: User | undefined = undefined
  @observable loginToken: AsyncState<string | undefined> = createAsyncState(undefined)

  constructor(private apiClient: ApiClient) {
  }

  @action.bound
  loadUsers() {
    return runWithAsyncState(this.availableUsers, () => fetchAvailableUsers())
  }

  @action.bound
  loadLoginToken() {
    return runWithAsyncState(this.loginToken, () => this.apiClient.fetchLoginToken())
  }
}
