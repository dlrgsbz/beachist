import { AsyncState, Result, createAsyncState, runWithAsyncState } from '../../lib'
import { action, observable } from 'mobx'

import { AdminFieldService } from 'services'
import { Field } from 'dtos'

export class AdminFieldStore {
  @observable fieldsState: AsyncState<Field[]> = createAsyncState([])

  constructor(private readonly adminFieldService: AdminFieldService) {}

  @action.bound
  async fetchFields(): Promise<Result<Error, Field[]>> {
    return runWithAsyncState(this.fieldsState, () => this.adminFieldService.getFields())
  }
}
