from rest_framework import serializers

from .mixins import CamelCaseMixin
from .models import Station, User, CrewInfo, Entry, SpecialEvent, Field, StationProvisioningRequest, StationField, \
    MqttMessage


class StationSerializer(CamelCaseMixin, serializers.ModelSerializer):
    class Meta:
        model = Station
        fields = '__all__'

class UserSerializer(CamelCaseMixin, serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['name', 'description']

class UserLoggedInSerializer(CamelCaseMixin, serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['name', 'description', 'roles']

class CrewInfoSerializer(CamelCaseMixin, serializers.ModelSerializer):
    class Meta:
        model = CrewInfo
        fields = ['date', 'station_id', 'crew']

class EntrySerializer(CamelCaseMixin, serializers.ModelSerializer):
    class Meta:
        model = Entry
        fields = '__all__'

class SpecialEventSerializer(CamelCaseMixin, serializers.ModelSerializer):
    class Meta:
        model = SpecialEvent
        fields = '__all__'

class FieldSerializer(CamelCaseMixin, serializers.ModelSerializer):
    class Meta:
        model = Field
        fields = '__all__'

class StationFieldSerializer(CamelCaseMixin, serializers.ModelSerializer):
    class Meta:
        model = StationField
        fields = '__all__'

class StationProvisioningRequestSerializer(CamelCaseMixin, serializers.ModelSerializer):
    class Meta:
        model = StationProvisioningRequest
        fields = '__all__'

class MqttMessageSerializer(CamelCaseMixin, serializers.ModelSerializer):
    class Meta:
        model = MqttMessage
        fields = '__all__'
