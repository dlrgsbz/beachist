import dataclasses
import enum
import json
import logging
from dataclasses import dataclass
from datetime import datetime, timezone, date
from typing import Optional
from uuid import UUID, uuid4

from django.core.serializers.json import DjangoJSONEncoder
from django.db.models import Q
from paho.mqtt import client as mqtt

from beachist.models import AppInfo, Station, StationField, CrewInfo, Event, SpecialEvent, Field, Entry

logger = logging.getLogger(__file__)


class ValidationMixin:
    def __post_init__(self):
        fields = dataclasses.fields(self)

        for field in fields:
            if not type(getattr(self, field.name)) == field.type:
                if field.type == date:
                    setattr(self, field.name, date.fromisoformat(getattr(self, field.name)))
                    continue
                if field.type == datetime:
                    setattr(self, field.name, datetime.fromisoformat(getattr(self, field.name)))
                    continue
                if issubclass(field.type, enum.Enum):
                    value = getattr(self, field.name)
                    enum_value = list(field.type)[field.type.values.index(value)]
                    setattr(self, field.name, enum_value)
                    continue
                if field.type == UUID:
                    setattr(self, field.name, UUID(getattr(self, field.name)))
                    continue
                if field.type == bool:
                    setattr(self, field.name, bool(getattr(self, field.name)))
                    continue
                raise ValueError(f'Field {field.name} must be of type {field.type}')


@dataclass
class StationPayload:
    stationId: UUID


@dataclass
class ConnectedChangedPayload:
    connected: bool
    appVersionCode: Optional[int] = None
    appVersion: Optional[str] = None

@dataclass
class UpdateCrewPayload(ValidationMixin):
    crew: str
    date: date

@dataclass
class CreateEventPayload(ValidationMixin):
    type: Event.EventType
    id: UUID
    date: datetime

@dataclass
class CreateSpecialEventPayload(ValidationMixin):
    id: UUID
    date: datetime
    note: str
    title: str
    kind: SpecialEvent.SpecialEventType
    notifier: str

@dataclass
class CreateEntryPayload(ValidationMixin):
    state: bool
    stateKind: Entry.StateKind
    amount: int
    note: str
    crew: str

    def __post_init__(self):
        super().__post_init__()

        # todo: validate statekind <-> amount/note mapping
        if self.amount < 0:
            raise ValueError('Amount must be positive')

def _validate_station_id(station_id: UUID) -> Optional[UUID]:
    station = Station.objects.filter(id=station_id).first()
    if not station:
        logger.warning(f'Station {station_id} not found')
        return None

    return station.id


def _get_uuid_id_from_topic(topic, idx):
    topic = topic.split('/')
    station_id = topic[idx]
    if not station_id:
        return None

    # todo: validate uuid

    return station_id


def connected_changed(mqtt_client, topic, message):
    payload = ConnectedChangedPayload(**message)

    station_id = _get_uuid_id_from_topic(topic, 0)
    station_id = _validate_station_id(station_id)
    if not station_id:
        return

    now = datetime.now(timezone.utc)

    latest = AppInfo.objects.filter(station_id=station_id).latest('date')

    event = AppInfo(
        date=now,
        station_id=station_id,
        version=payload.appVersion or latest.version,
        version_code=payload.appVersionCode or latest.version_code,
        online=payload.connected,
    )
    event.save()


def get_fields(mqtt_client: mqtt.Client, topic, *args):
    station_id = _get_uuid_id_from_topic(topic, 0)
    if not station_id:
        logger.warning(f'Could not extract station id from topic {topic}')
        return
    if not _validate_station_id(station_id):
        return

    # load fields from db
    fields = StationField.objects.filter(
        Q(station_id=station_id) | Q(station_id__isnull=True)
    ).all().order_by('field__sort_id')

    result = [
        {
            'id': field.field.id,
            'name': field.field.name,
            'parent': field.field.parent_id,
            'required': field.required,
            'note': field.note,
            # todo: get entry
            'entry': None,
        }
        for field in fields
    ]

    # publish
    mqtt_client.publish(f'fields/{station_id}', json.dumps(result, cls=DjangoJSONEncoder))

def update_crew(mqtt_client, topic, message):
    station_id = _get_uuid_id_from_topic(topic, 0)
    if not station_id:
        logger.warning(f'Could not extract station id from topic {topic}')
        return
    station_id = _validate_station_id(station_id)
    if not station_id:
        return

    payload = UpdateCrewPayload(**message)

    crew_info = CrewInfo(crew=payload.crew, date=payload.date, station_id=station_id)
    crew_info.save()

def create_event(mqtt_client, topic, message):
    station_id = _get_uuid_id_from_topic(topic, 0)
    if not station_id:
        logger.warning(f'Could not extract station id from topic {topic}')
        return
    station_id = _validate_station_id(station_id)
    if not station_id:
        return

    payload = CreateEventPayload(**message)

    event = Event(
        id=payload.id,
        type=payload.type,
        date=payload.date,
        station_id=station_id,
    )
    event.save()

    mqtt_client.publish(f'events/{station_id}', str(event.id))

def create_special_event(mqtt_client, topic, message):
    station_id = _get_uuid_id_from_topic(topic, 0)
    if not station_id:
        logger.warning(f'Could not extract station id from topic {topic}')
        return
    station_id = _validate_station_id(station_id)
    if not station_id:
        return

    payload = CreateSpecialEventPayload(**message)

    special_event = SpecialEvent(
        id=payload.id,
        date=payload.date,
        type=payload.kind,
        notifier=payload.notifier,
        title=payload.title,
        note=payload.note,
        station_id=station_id,
    )
    special_event.save()

    mqtt_client.publish(f'special-event/{station_id}/success', str(special_event.id))

def create_entry(mqtt_client, topic, message):
    # topic: +/field/+/entry
    station_id = _get_uuid_id_from_topic(topic, 0)
    if not station_id:
        logger.warning(f'Could not extract station id from topic {topic}')
        return
    station_id = _validate_station_id(station_id)
    if not station_id:
        return

    field_id = _get_uuid_id_from_topic(topic, 2)
    if not field_id:
        logger.warning(f'Could not extract field id from topic {topic}')
        return
    field = Field.objects.filter(id=field_id).first()
    if not field:
        logger.warning(f'Field {field_id} not found')
        return None

    payload = CreateEntryPayload(**message)

    entry = Entry(
        id=uuid4(),
        field_id=field_id,
        state=payload.state,
        state_kind=payload.stateKind,
        amount=payload.amount,
        note=payload.note,
        date=datetime.now(timezone.utc),
        crew=payload.crew,
        station_id=station_id,
    )
    entry.save()
