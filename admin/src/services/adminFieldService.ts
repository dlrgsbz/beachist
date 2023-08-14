import { ApiClient } from '../modules/data'
import { Field } from '../dtos'

export class AdminFieldService {
  constructor(private readonly apiClient: ApiClient) {}

  public async getFields(): Promise<Field[]> {
    return this.apiClient.fetchFields()
  }
}
