import { type IotClient, iotClient } from "../../aws/iot";
import { type SSMClient, ssmClient } from "../../aws/ssm";

import axios from "axios";
import { logger } from "../../logger";

export const getWaterTempHandler = (): Promise<void> => {
  return getWaterTemp(iotClient, ssmClient)
}

export const getWaterTemp = async (iotClient: IotClient, ssmClient: SSMClient): Promise<void> => {
  const url = "https://www2.bsh.de/aktdat/bum/bum_data.json"

  const response = await axios.get<BshResponse>(url, {
    headers: {
      'Referer': 'https://www2.bsh.de/aktdat/bum/bum31.html',
      'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/116.0',
    }
  })

  const stationId= await ssmClient.getParameter('/beachist/bsh/station-id')
  const stationInfo = response.data[stationId]

  const sstdata = stationInfo.sstdata
  const timestamp = new Date(sstdata[0][0]).toISOString()
  const waterTempData = sstdata[1]

  const waterTemp = waterTempData[waterTempData.length - 1]
  const data = {
    waterTemp,
    timestamp,
  }

  const topic = 'shared/weather/water'

  logger.debug(`got water temp: ${waterTemp}ºC, publishing to ${topic}`)

  await iotClient.publish(topic, JSON.stringify(data), true)
}

interface BshResponse {
  [stationId: string]: BshStationInfo
}

interface BshStationInfo {
  area: string
  displayname: string
  sst: 0 | 1
  sstdata: [
    string[], // dates
    number[], // temperatures for first entry in dates
    number[], // temperatures for second entry in dates
  ]
}
