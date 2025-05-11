# This is an auto-generated Django model module.
# You'll have to do the following manually to clean this up:
#   * Rearrange models' order
#   * Make sure each model has one field with primary_key=True
#   * Make sure each ForeignKey and OneToOneField has `on_delete` set to the desired behavior
#   * Remove `managed = False` lines if you wish to allow Django to create, modify, and delete the table
# Feel free to rename the models, but don't rename db_table values or field names.
import base64
from datetime import datetime
from typing import Union
from uuid import UUID

from django.contrib.auth.base_user import AbstractBaseUser
from django.contrib.auth.models import AbstractUser, AnonymousUser, UserManager
from django.db import models


class AppInfo(models.Model):
    pk = models.CompositePrimaryKey('station_id', 'date')
    date = models.DateTimeField()
    # todo: station_id = models.ForeignKey(Station, on_delete=models.CASCADE)
    station_id = models.UUIDField(max_length=36, db_comment='(DC2Type:uuid)')
    version = models.CharField(max_length=255, db_collation='utf8mb4_unicode_ci')
    version_code = models.IntegerField(blank=True, null=True)
    online = models.BooleanField()

    class Meta:
        db_table = 'app_info'


class CrewInfo(models.Model):
    pk = models.CompositePrimaryKey('station_id', 'date')
    date = models.CharField(max_length=10)
    # todo: station_id = models.ForeignKey(Station, on_delete=models.CASCADE)
    station_id = models.UUIDField(max_length=36, db_comment='(DC2Type:uuid)')
    crew = models.CharField(max_length=255)

    class Meta:
        db_table = 'crew_info'
        unique_together = (('station_id', 'date'),)


class Field(models.Model):
    id = models.UUIDField(primary_key=True, max_length=64)
    name = models.CharField(max_length=265)
    parent_id = models.UUIDField(max_length=64, blank=True, null=True)
    sort_id = models.IntegerField(blank=True, null=True)

    class Meta:
        db_table = 'field'


class Station(models.Model):
    id = models.UUIDField(primary_key=True, max_length=64)
    name = models.CharField(max_length=64)
    sort_id = models.IntegerField(blank=True, null=True)

    class Meta:
        db_table = 'station'


class StationField(models.Model):
    id = models.UUIDField(primary_key=True, max_length=64)
    # one_to_one
    station = models.ForeignKey(Station, on_delete=models.CASCADE, null=True)
    field = models.ForeignKey(Field, on_delete=models.CASCADE)
    required = models.IntegerField(blank=True, null=True)
    note = models.TextField(blank=True, null=True)

    class Meta:
        db_table = 'station_field'


class Entry(models.Model):
    class StateKind(models.TextChoices):
        BROKEN = 'broken', 'Defekt'
        TOO_LITTLE = 'tooLittle', 'Zu wenig'
        OTHER = 'other', 'Andere'

    id = models.UUIDField(primary_key=True, max_length=64)
    station = models.ForeignKey(Station, on_delete=models.CASCADE)
    field = models.ForeignKey(Field, on_delete=models.CASCADE)
    state = models.IntegerField()
    state_kind = models.CharField(max_length=10, blank=True, null=True, choices=StateKind.choices)
    amount = models.IntegerField(blank=True, null=True)
    note = models.CharField(max_length=256, blank=True, null=True)
    date = models.DateTimeField()
    crew = models.CharField(max_length=255, blank=True, null=True)

    class Meta:
        db_table = 'entry'


class Event(models.Model):
    class EventType(models.TextChoices):
        FIRST_AID = 'firstAid', 'Erste Hilfe'
        SEARCH = 'search', 'Suchmeldung'

    id = models.UUIDField(primary_key=True, max_length=36, db_comment='(DC2Type:uuid)')
    station = models.ForeignKey(Station, on_delete=models.CASCADE)
    type = models.CharField(
        max_length=255,
        blank=True,
        null=True,
        choices=EventType.choices,
    )
    date = models.DateTimeField()

    class Meta:
        db_table = 'event'


class SpecialEvent(models.Model):
    class SpecialEventType(models.TextChoices):
        DAMAGE = 'damage', 'Schadenmeldung'
        EVENT = 'event', 'Besonderes Vorkommnis'

    id = models.UUIDField(primary_key=True, max_length=36,
                          db_comment='(DC2Type:uuid)')
    station = models.ForeignKey(Station, on_delete=models.CASCADE)
    title = models.CharField(max_length=255, db_collation='utf8mb4_unicode_ci')
    note = models.TextField(db_collation='utf8mb4_unicode_ci')
    date = models.DateTimeField()
    notifier = models.CharField(max_length=64, db_collation='utf8mb4_unicode_ci')
    type = models.CharField(max_length=6, db_collation='utf8mb4_unicode_ci')

    class Meta:
        db_table = 'special_event'


class StationProvisioningRequest(models.Model):
    station = models.ForeignKey(Station, on_delete=models.CASCADE)
    password = models.CharField(max_length=255)
    expires_at = models.DateTimeField()
    active = models.BooleanField()

    class Meta:
        db_table = 'station_provisioning_request'


class User(AbstractBaseUser, models.Model):
    USERNAME_FIELD = 'name'

    name = models.CharField(max_length=255, unique=True)
    description = models.CharField(max_length=255)
    password = models.CharField(max_length=255)
    roles = models.JSONField(db_comment='(DC2Type:json)')

    @property
    def is_staff(self):
        return 'ROLE_ADMIN' in self.roles

    objects = UserManager()

    class Meta:
        db_table = 'user'


class ProvisioningUser(AnonymousUser):
    station_id: UUID

class MqttMessage(models.Model):
    class MqttPayloadType(models.IntegerChoices):
        PLAIN = 0
        BINARY = 1

    topic: str = models.CharField(max_length=255)
    payload: str = models.CharField(max_length=255)
    payload_type = models.IntegerField(choices=MqttPayloadType)
    qos: int = models.IntegerField()
    retain: bool = models.BooleanField()
    ts: datetime = models.DateTimeField()

    @property
    def decoded_payload(self):
        if self.payload_type == MqttMessage.MqttPayloadType.PLAIN:
            return self.payload
        return base64.b64decode(self.payload)

    def encode_payload(self, payload: Union[str, bytes]):
        if type(payload) == bytes:
            # payload = base64.b64encode(payload).decode('utf-8')
            # self.payload_type = MqttMessage.MqttPayloadType.BINARY
            payload = payload.decode('utf-8')
            self.payload_type = MqttMessage.MqttPayloadType.PLAIN
        else:
            self.payload_type = MqttMessage.MqttPayloadType.PLAIN
        self.payload = payload
