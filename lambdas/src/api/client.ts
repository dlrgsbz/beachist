import { CrewInfo, IdResponse, SpecialEvent } from './dtos'

import axios from 'axios'
import { config } from '../config'

export interface ApiClient {
  createSpecialEvent: (stationId: string, event: SpecialEvent) => Promise<string>
  updateCrew: (stationId: string, crew: CrewInfo) => Promise<void>
}

class ApiClientImpl implements ApiClient {
  constructor(private readonly baseUrl: string) {}

  async createSpecialEvent(stationId: string, event: SpecialEvent): Promise<string> {
    const data = await axios.post<IdResponse>(`${this.baseUrl}station/${stationId}/special`, event)
    return data.data.id
  }

  async updateCrew(stationId: string, crew: CrewInfo): Promise<void> {
    await axios.post(`${this.baseUrl}station/${stationId}/crew`, crew)
  }
}

export const apiClient: ApiClient = new ApiClientImpl(config.BACKEND_URL)
