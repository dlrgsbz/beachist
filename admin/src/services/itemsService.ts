import { Field, FieldAssignment, StationInfo } from 'dtos'

import { ApiClient } from 'modules/data'

export interface ItemPayload {
  name: string
  sortId?: number | null
  parent?: string | null
}

export class ItemsService {
  constructor(private apiClient: ApiClient) {}

  public getItems(): Promise<Field[]> {
    return this.apiClient.fetchFields()
  }

  public getStations(): Promise<StationInfo[]> {
    return this.apiClient.fetchStations()
  }

  public getAssignments(): Promise<FieldAssignment[]> {
    return this.apiClient.fetchAssignments()
  }

  public createItem(payload: ItemPayload): Promise<Field> {
    return this.apiClient.createField(payload)
  }

  public updateItem(id: string, payload: ItemPayload): Promise<Field> {
    return this.apiClient.updateField(id, payload)
  }

  public deleteItem(id: string): Promise<void> {
    return this.apiClient.deleteField(id)
  }

  public setStationAssignment(fieldId: string, stationId: string, assigned: boolean): Promise<void> {
    return assigned
      ? this.apiClient.assignFieldToStation(stationId, fieldId)
      : this.apiClient.unassignFieldFromStation(stationId, fieldId)
  }

  public setGlobalAssignment(fieldId: string, assigned: boolean): Promise<void> {
    return assigned ? this.apiClient.assignFieldGlobally(fieldId) : this.apiClient.unassignFieldGlobally(fieldId)
  }

  public reorderItems(orderedIds: string[]): Promise<Field[]> {
    return this.apiClient.reorderFields(orderedIds)
  }

  public saveAssignments(
    fieldId: string,
    global: boolean,
    stationIds: string[],
    required?: number | null,
    note?: string | null,
  ): Promise<void> {
    return this.apiClient.setFieldAssignments(fieldId, global, stationIds, required, note)
  }
}
