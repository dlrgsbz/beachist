import axios from 'axios'

export interface HttpHeaders {
  [header: string]: string
}

export interface HttpOptions {
  headers?: HttpHeaders
  params?: object
}

export interface HttpResponse {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  data: any
  status: number
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export async function httpGet(url: string, options?: HttpOptions): Promise<HttpResponse> {
  try {
    const response = await axios.get(url, options)
    return { data: response.data, status: response.status }
  } catch (error) {
    throw error
  }
}

export async function httpPost(url: string, data?: object, options?: HttpOptions): Promise<HttpResponse> {
  try {
    const response = await axios.post(url, data, options)
    return { data: response.data, status: response.status }
  } catch (error) {
    throw error
  }
}
