import { AsyncState, Result, createAsyncState, isSuccessful, runWithAsyncState } from 'lib'
import { Field, FieldAssignment, StationInfo } from 'dtos'
import { ItemPayload, ItemsService } from 'services'
import { action, makeObservable, observable, runInAction } from 'mobx'

class ItemsStore {
  @observable itemsState: AsyncState<Field[]> = createAsyncState<Field[]>([])
  @observable stationsState: AsyncState<StationInfo[]> = createAsyncState<StationInfo[]>([])
  @observable assignmentsState: AsyncState<FieldAssignment[]> = createAsyncState<FieldAssignment[]>([])
  @observable mutationState: AsyncState<unknown> = createAsyncState<unknown>(undefined)

  constructor(private itemsService: ItemsService) {
    makeObservable(this)
  }

  @action.bound
  async fetchAll(): Promise<void> {
    await Promise.all([this.fetchItems(), this.fetchStations(), this.fetchAssignments()])
  }

  @action.bound
  async fetchItems(): Promise<Result<Error, Field[]>> {
    return runWithAsyncState(this.itemsState, () => this.itemsService.getItems())
  }

  @action.bound
  async fetchStations(): Promise<Result<Error, StationInfo[]>> {
    return runWithAsyncState(this.stationsState, () => this.itemsService.getStations())
  }

  @action.bound
  async fetchAssignments(): Promise<Result<Error, FieldAssignment[]>> {
    return runWithAsyncState(this.assignmentsState, () => this.itemsService.getAssignments())
  }

  @action.bound
  async createItem(payload: ItemPayload): Promise<Result<Error, unknown>> {
    const result = await runWithAsyncState(this.mutationState, () => this.itemsService.createItem(payload))
    if (isSuccessful(result)) {
      await this.fetchItems()
    }
    return result
  }

  @action.bound
  async updateItem(id: string, payload: ItemPayload): Promise<Result<Error, unknown>> {
    const result = await runWithAsyncState(this.mutationState, () => this.itemsService.updateItem(id, payload))
    if (isSuccessful(result)) {
      await this.fetchItems()
    }
    return result
  }

  @action.bound
  async deleteItem(id: string): Promise<Result<Error, unknown>> {
    const result = await runWithAsyncState(this.mutationState, () => this.itemsService.deleteItem(id))
    if (isSuccessful(result)) {
      await Promise.all([this.fetchItems(), this.fetchAssignments()])
    }
    return result
  }

  @action.bound
  async toggleStationAssignment(
    fieldId: string,
    stationId: string,
    assigned: boolean,
  ): Promise<Result<Error, unknown>> {
    const result = await runWithAsyncState(this.mutationState, () =>
      this.itemsService.setStationAssignment(fieldId, stationId, assigned),
    )
    if (isSuccessful(result)) {
      await this.fetchAssignments()
    }
    return result
  }

  @action.bound
  async toggleGlobalAssignment(fieldId: string, assigned: boolean): Promise<Result<Error, unknown>> {
    const result = await runWithAsyncState(this.mutationState, () =>
      this.itemsService.setGlobalAssignment(fieldId, assigned),
    )
    if (isSuccessful(result)) {
      await this.fetchAssignments()
    }
    return result
  }

  @action.bound
  async reorderItems(orderedIds: string[]): Promise<Result<Error, unknown>> {
    const previous = this.itemsState.data
    const byId = new Map(previous.map(field => [field.id, field]))
    const optimistic = orderedIds.map(id => byId.get(id)).filter((field): field is Field => field !== undefined)

    runInAction(() => {
      this.itemsState.data = optimistic
    })

    const result = await runWithAsyncState(this.mutationState, async () => {
      const updated = await this.itemsService.reorderItems(orderedIds)
      runInAction(() => {
        this.itemsState.data = updated
      })
    })

    if (!isSuccessful(result)) {
      runInAction(() => {
        this.itemsState.data = previous
      })
    }
    return result
  }

  @action.bound
  async saveAssignments(
    fieldId: string,
    global: boolean,
    stationIds: string[],
    required?: number | null,
    note?: string | null,
  ): Promise<Result<Error, unknown>> {
    const result = await runWithAsyncState(this.mutationState, () =>
      this.itemsService.saveAssignments(fieldId, global, stationIds, required, note),
    )
    if (isSuccessful(result)) {
      await this.fetchAssignments()
    }
    return result
  }
}

export default ItemsStore
