import { type IotClient, iotClient } from "../../aws/iot";
import { type SSMClient, ssmClient } from "../../aws/ssm";

import axios from "axios";
import { logger } from "../../logger";

export const getWeatherHandler = (): Promise<void> => {
  return getWeather(iotClient, ssmClient)
}

export const getWeather = async (iotClient: IotClient, ssmClient: SSMClient): Promise<void> => {
  const stationId = await ssmClient.getParameter('/beachist/dwd/station-id')
  const url = `https://s3.eu-central-1.amazonaws.com/app-prod-static.warnwetter.de/v16/current_measurement_${stationId}.json`

  const response = await axios.get<DwdResponse>(url)

  const { data } = response

  const timestamp = new Date(data.time).toISOString()

  const weatherInfo = {
    temperature: Math.round(data.temperature / 10),
    windBft: windTenthKmhToBft(data.meanwind),
    windDirection: windDirectionFromDegree(data.winddirection),
    timestamp,
  }

  const topic = 'shared/weather/air'

  logger.debug(`got weather info: ${JSON.stringify(weatherInfo)}, publishing to ${topic}`)

  await iotClient.publish(topic, JSON.stringify(weatherInfo), true)
}

const windDirectionFromDegree = (degree: number): string => {
  if (degree < 22.5) {
    return 'Nord'
  }
  if (degree < 67.5) {
    return 'Nord-Ost'
  }
  if (degree < 112.5) {
    return 'Ost'
  }
  if (degree < 157.5) {
    return 'Süd-Ost'
  }
  if (degree < 202.5) {
    return 'Süd'
  }
  if (degree < 247.5) {
    return 'Süd-West'
  }
  if (degree < 292.5) {
    return 'West'
  }
  if (degree < 337.5) {
    return 'Nord-West'
  }
  return 'Nord'
}

/**
 *
 * @param wind in km/h * 10
 * @returns wind in bft
 */
const windTenthKmhToBft = (wind: number): number => {
  const wind_in_kmh = wind / 10

  // return wind strength in bft
  if (wind_in_kmh < 1) {
    return 0
  }
  if (wind_in_kmh < 6) {
    return 1
  }
  if (wind_in_kmh < 12) {
    return 2
  }
  if (wind_in_kmh < 20) {
    return 3
  }
  if (wind_in_kmh < 29) {
    return 4
  }
  if (wind_in_kmh < 39) {
    return 5
  }
  if (wind_in_kmh < 50) {
    return 6
  }
  if (wind_in_kmh < 62) {
    return 7
  }
  if (wind_in_kmh < 75) {
    return 8
  }
  if (wind_in_kmh < 89) {
    return 9
  }
  if (wind_in_kmh < 103) {
    return 10
  }
  if (wind_in_kmh < 118) {
    return 11
  }
  return 12
}

interface DwdResponse {
  precipitation3h: number
  totalsnow: number
  dewpoint: number
  sunshine: number
  cloud_cover_total: number
  icon: number
  pressure: number
  history: unknown
  meanwind: number
  maxwind: number
  winddirection: number
  precipitation: number
  temperature: number
  humidity: number
  time: number
}
