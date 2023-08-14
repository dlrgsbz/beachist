import { AdminFieldService } from './adminFieldService'
import { ApiClient } from '../modules/data'
import { mock } from 'jest-mock-extended'

describe('getFields', () => {
  const mockApiClient = mock<ApiClient>()
  const adminFieldService = new AdminFieldService(mockApiClient)

  it('should call apiClient method', async () => {
    mockApiClient.fetchFields.mockReturnValue(Promise.resolve([]))

    const fields = adminFieldService.getFields()

    expect(fields).toEqual([])
    expect(mockApiClient.fetchFields).toHaveBeenCalledTimes(1)
  })
})
