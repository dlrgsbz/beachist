import { IotClient, iotClient } from "../../aws/iot";
import { SSMClient, ssmClient } from "../../aws/ssm";

import axios from "axios";
import { formatIso8601DateTime } from "../../util";
import { logger } from "../../logger";

export const getUviHandler = async (): Promise<void> => {
    await handler(ssmClient, iotClient)
}

// should handle cron aws lambda event
export const handler = async (ssmClient: SSMClient, iotClient: IotClient): Promise<void> => {
    const { lat, lng, openUvAccessToken } = await getConfig(ssmClient)

    const response = await axios.get<OpenUVResponse>(`https://api.openuv.io/api/v1/uv?lat=${lat}&lng=${lng}&alt=0`, {
        headers: {
            'x-access-token': openUvAccessToken,
        },
    })

    const { data } = response

    const uvInfo = {
        uv: data.result.uv,
        maxUv: data.result.uv_max,
        timestamp: formatIso8601DateTime(new Date(data.result.uv_time)),
    }

    const topic = 'shared/weather/uvi'

    logger.debug(`got uv info: ${JSON.stringify(uvInfo)}, publishing to ${topic}`)

    await iotClient.publish(topic, JSON.stringify(uvInfo), true)
}

interface GetUviConfig {
    lat: number
    lng: number
    openUvAccessToken: string
}

const getConfig = async (ssmClient: SSMClient): Promise<GetUviConfig> => {
    const parameters = await ssmClient.getParameters(['/beachist/openuv/location', '/beachist/openuv/api-key'])

    if (!parameters['/beachist/openuv/location']) {
        throw Error(`Missing parameter /beachist/openuv/location`)
    }
    if (!parameters['/beachist/openuv/api-key']) {
        throw Error(`Missing parameter /beachist/openuv/api-key`)
    }

    const [lat, lng] = JSON.parse(parameters['/beachist/openuv/location'])

    return {
        lat, lng,
        openUvAccessToken: parameters['/beachist/openuv/api-key'],
    }
}

interface OpenUVResponse {
    result: {
        uv: number
        uv_max: number
        uv_time: string
    }
}
