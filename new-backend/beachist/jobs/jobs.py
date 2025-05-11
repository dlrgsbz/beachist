import datetime
import json
import logging
import math
from dataclasses import dataclass
from typing import Callable, Optional
from zoneinfo import ZoneInfo

import requests

from beachist.settings import RAPIDAPI_API_KEY, WEATHER_LNG, WEATHER_LAT, OPENUV_API_KEY, WARNWETTER_STATION_ID

logger = logging.getLogger(__name__)

@dataclass
class Job:
    name: str
    run: Callable
    dow: Optional[str]
    dom: Optional[str]
    hour: Optional[str]
    minute: Optional[str]
    second: Optional[str]


def get_uvi_job(mqttclient):
    # todo: store both in config
    openuv_api_key = OPENUV_API_KEY
    lat, lng = WEATHER_LAT, WEATHER_LNG
    url = f'https://api.openuv.io/api/v1/uv?lat={lat}&lng={lng}&alt=0'

    response = requests.get(url, headers={'x-access-token': openuv_api_key})

    if response.status_code != 200:
        logger.error(f'OpenUV API returned status code {response.status_code}')
        return

    data = response.json()

    date = datetime.datetime.fromisoformat(data['result']['uv_time'])

    uv_info = {
        'uv': data['result']['uv'],
        'maxUv': data['result']['uv_max'],
        'timestamp': date.isoformat(timespec='seconds').replace("+00:00", "Z"),
    }

    topic = 'shared/weather/uvi'

    logger.debug(f'got uv info: {json.dumps(uv_info)}, publishing to {topic}')

    mqttclient.publish(topic, json.dumps(uv_info), retain=True)


def wind_tenth_kmh_to_bft(wind):
    wind_in_kmh = wind / 10

    if wind_in_kmh < 1:
        return 0
    if wind_in_kmh < 6:
        return 1
    if wind_in_kmh < 12:
        return 2
    if wind_in_kmh < 20:
        return 3
    if wind_in_kmh < 29:
        return 4
    if wind_in_kmh < 39:
        return 5
    if wind_in_kmh < 50:
        return 6
    if wind_in_kmh < 62:
        return 7
    if wind_in_kmh < 75:
        return 8
    if wind_in_kmh < 89:
        return 9
    if wind_in_kmh < 103:
        return 10
    if wind_in_kmh < 118:
        return 11
    return 12


def wind_direction_from_degree(degree):
    if degree < 22.5:
        return 'Nord'
    if degree < 67.5:
        return 'Nord-Ost'
    if degree < 112.5:
        return 'Ost'
    if degree < 157.5:
        return 'Süd-Ost'
    if degree < 202.5:
        return 'Süd'
    if degree < 247.5:
        return 'Süd-West'
    if degree < 292.5:
        return 'West'
    if degree < 337.5:
        return 'Nord-West'
    return 'Nord'


def get_weather_job(mqttclient):
    station_id = WARNWETTER_STATION_ID
    url = f'https://s3.eu-central-1.amazonaws.com/app-prod-static.warnwetter.de/v16/current_measurement_{station_id}.json'

    response = requests.get(url)

    if response.status_code != 200:
        logger.error(f'DWD API returned status code {response.status_code}')
        return

    data = response.json()

    date = datetime.datetime.fromtimestamp(data['time'] / 1000)
    zone_info = ZoneInfo('Europe/Berlin')
    date = date.replace(tzinfo=zone_info).astimezone(datetime.timezone.utc)

    timestamp = date.isoformat(timespec='seconds').replace("+00:00", "Z")

    weather_info = {
        'temperature': round(data['temperature'] / 10),
        'windBft': wind_tenth_kmh_to_bft(data['meanwind']),
        'windDirection': wind_direction_from_degree(data['winddirection']),
        'timestamp': timestamp,
    }

    topic = 'shared/weather/air'

    logger.debug(f'got weather info: {json.dumps(weather_info)}, publishing to {topic}')

    mqttclient.publish(topic, json.dumps(weather_info), retain=True)

# todo: move back to BSH API if it ever comes back 🥲
def get_water_temp_job(mqttclient):
    lat, lng = WEATHER_LAT, WEATHER_LNG
    url = "https://sea-surface-temperature.p.rapidapi.com/current"
    querystring = {"latlon": f"{lat},{lng}"}

    headers = {
        "x-rapidapi-key": RAPIDAPI_API_KEY,
        "x-rapidapi-host": "sea-surface-temperature.p.rapidapi.com"
    }

    response = requests.get(url, headers=headers, params=querystring)

    if response.status_code != 200:
        logger.error(f'BSH API returned status code {response.status_code}')
        return

    data = response.json()

    water_data = None

    for datum in data:
        if datum['date'] == datetime.datetime.now().strftime('%Y%m%d'):
            water_data = {
                'waterTemp': datum['tempC'],
                'timestamp': datetime.datetime.now(tz=datetime.timezone.utc).isoformat(timespec='seconds').replace("+00:00", "Z")
            }
            break

    if water_data is None:
        logger.warning(f'BSH API returned no data')
        return

    topic = 'shared/weather/water'

    logger.debug(f'got water temperature: {water_data['waterTemp']}ºC, publishing to {topic}')

    mqttclient.publish(topic, json.dumps(water_data), retain=True)


jobs = [
    Job('get_uvi', get_uvi_job, None, None, '7-18', '47', '0'),
    Job('get_weather', get_weather_job, None, None, '7-18', '45', '0'),
    Job('get_water_temp', get_water_temp_job, None, None, '7,11,15', '49', '0'),
]
