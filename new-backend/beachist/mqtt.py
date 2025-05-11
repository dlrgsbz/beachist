import asyncio
import json
import logging
import uuid
from datetime import datetime, timezone
from typing import List, Tuple, Callable, Union

from paho.mqtt import client as mqtt
from paho.mqtt.properties import Properties
from paho.mqtt.subscribeoptions import SubscribeOptions

from beachist.event_handlers import connected_changed, get_fields, update_crew, create_event, create_special_event, \
    create_entry
from beachist.jobs import schedule_jobs, unschedule_jobs
from beachist.models import MqttMessage
from beachist.settings import MQTT_HOST, MQTT_PORT, MQTT_KEEPALIVE, MQTT_USERNAME, MQTT_PASSWORD, MQTT_USE_TLS, DEBUG

logger = logging.getLogger(__name__)

topics = [
    ('+/connected/update', True, connected_changed),
    ('+/field/get', False, get_fields),
    ('+/crew', True, update_crew),
    ('+/event', True, create_event),
    ('+/special-event', True, create_special_event),
    ('+/field/+/entry', True, create_entry),
]


class MqttClient:
    client: mqtt.Client = None
    topics: List[Tuple[str, bool, Callable]] = topics

    def _on_connect(self, mqtt_client: mqtt.Client, userdata, flags, rc):
        if rc == 0:
            logger.info('Connected successfully')
            self._subscribe_to_topics()

            schedule_jobs(self)
        else:
            logger.info('Bad connection. Code:', rc)

    def _subscribe_to_topics(self):
        if DEBUG:
            logger.debug('subscribing to all topics')
            self.client.subscribe('#', options=SubscribeOptions(noLocal=True))
            return
        for topic, _, _ in topics:
            logger.info(f'Subscribing to {topic}')
            self.client.subscribe(topic)

    def on_disconnect(self, client, userdata, rc):
        unschedule_jobs()
        # client.unsubscribe('+/field/+/entry')
        logger.warning("Disconnected from MQTT Broker")

    def _get_callback(self, for_topic):
        for topic, has_message, callback in self.topics:
            split = topic.split('/')
            split_for_topic = for_topic.split('/')
            if len(split) != len(split_for_topic):
                continue

            matchable_topic = '/'.join(['+' if split[idx] == '+' else item for idx, item in enumerate(split_for_topic)])
            if matchable_topic == topic:
                return has_message, callback

        return None

    def on_message(self, mqtt_client, userdata, msg: mqtt.MQTTMessage):
        logger.debug(f'Received message on topic: {msg.topic} with payload: {msg.payload}')
        self._write_message_to_db(msg.topic, msg.payload, msg.qos, msg.retain)
        try:
            payload = None
            result = self._get_callback(msg.topic)
            if result is not None:
                has_message, callback = result
                if has_message:
                    payload = json.loads(msg.payload)
                callback(self, msg.topic, payload)
            else:
                logger.warning(f'No callback found for topic: {msg.topic}')
        except json.decoder.JSONDecodeError:
            logger.error(f'Message decode error for message {msg.payload} on {msg.topic}')
        except Exception as e:
            logger.error(f'Couldn\'t run the callback for message {msg.payload} on {msg.topic}: {e}')

    def publish(self,
                topic: str,
                payload: Union[str, bytes] = None,
                qos: int = 0,
                retain: bool = False,
                properties: Properties | None = None,
                ):
        self.client.publish(topic, payload, qos, retain, properties)
        self._write_message_to_db(topic, payload, qos, retain)

    def _write_message_to_db(self,
                             topic: str,
                             payload: Union[str, bytes] = None,
                             qos: int = 0,
                             retain: bool = False,
                             ):
        ts = datetime.now(tz=timezone.utc)

        message = MqttMessage(topic=topic, qos=qos, retain=retain, ts=ts)
        message.encode_payload(payload)
        try:
            asyncio.run(self._real_save(message), debug=True)
        except Exception as e:
            logger.warning(f'Failed to save message: {e}')

    async def _real_save(self, message):
        try:
            await message.asave()
        except Exception as e:
            logger.warning(f'Failed to save message: {e}')

    def __init__(self):
        self.client = mqtt.Client(
            client_id=f'beachist-backend-{uuid.uuid4()}',
            reconnect_on_failure=True,
        )
        if DEBUG:
            self.client.enable_logger(logger)

    def start(self):
        logger.debug('Setting up MQTT client')
        self.client.on_connect = lambda client, userdata, flags, rc: self._on_connect(client, userdata, flags, rc)
        self.client.on_message = lambda client, userdata, msg: self.on_message(client, userdata, msg)
        self.client.on_disconnect = lambda client, userdata, rc: self.on_disconnect(client, userdata, rc)
        if MQTT_USERNAME is not None:
            self.client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
        if MQTT_USE_TLS:
            self.client.tls_set()
        logger.debug('Connecting to MQTT broker')
        self.client.connect(host=MQTT_HOST, port=MQTT_PORT, keepalive=MQTT_KEEPALIVE)

        self.client.loop_start()

        return self.client
