import { action, observable } from 'mobx'
import { AsyncState, createAsyncState, runWithAsyncState } from 'lib'
import { User, UserInfo } from 'dtos'
import { fetchAvailableUsers } from 'modules/data'

export class AuthStore {
  @observable availableUsers: AsyncState<UserInfo[]> = createAsyncState([])
  @observable currentUser: User | undefined = undefined

  @action.bound
  loadUsers() {
    return runWithAsyncState(this.availableUsers, () => fetchAvailableUsers())
  }
}
