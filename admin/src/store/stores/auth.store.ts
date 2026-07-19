import { ApiClient, fetchAvailableUsers } from 'modules/data'
import { AsyncState, createAsyncState, runWithAsyncState } from 'lib'
import { User, UserInfo } from 'dtos'
import { action, makeObservable, observable } from 'mobx'

export class AuthStore {
  @observable availableUsers: AsyncState<UserInfo[]> = createAsyncState([])
  @observable currentUser: User | undefined = undefined
  @observable loginToken: AsyncState<string | undefined> = createAsyncState(undefined)

  constructor(private apiClient: ApiClient) {
    makeObservable(this)
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
