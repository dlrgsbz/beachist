import logging
import secrets
import string
import time
from datetime import datetime, timedelta, timezone
from functools import reduce
from typing import List
from uuid import UUID

from django.db.models import Window, F
from django.db.models.functions import RowNumber
from rest_framework import status, authentication, exceptions
from rest_framework.authentication import get_authorization_header
from rest_framework.pagination import CursorPagination
from rest_framework.permissions import IsAuthenticated, IsAdminUser
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.authentication import JWTStatelessUserAuthentication

from .ca.certificates import generate_client_cert
from .models import Station, User, AppInfo, CrewInfo, Entry, Event, SpecialEvent, Field, StationProvisioningRequest, \
    ProvisioningUser, MqttMessage
from .serializers import StationSerializer, UserSerializer, CrewInfoSerializer, EntrySerializer, SpecialEventSerializer, \
    FieldSerializer, UserLoggedInSerializer, StationProvisioningRequestSerializer, MqttMessageSerializer

logger = logging.getLogger(__name__)


class StationListApiView(APIView):
    permission_classes = [IsAuthenticated]
    authentication_classes = [JWTStatelessUserAuthentication]

    def get(self, request, *args, **kwargs):
        """
        List all the stations
        """
        stations = Station.objects.all().order_by('sort_id')
        serializer = StationSerializer(stations, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class StationDetailApiView(APIView):
    permission_classes = [IsAuthenticated]
    authentication_classes = [JWTStatelessUserAuthentication]

    def get(self, request, *args, **kwargs):
        station = Station.objects.get(id=kwargs['id'])
        serializer = StationSerializer(station)
        return Response(serializer.data, status=status.HTTP_200_OK)


# todo: move business logic somewhere else
def get_app_info(station_id: UUID):
    # todo: running this function for every station takes about 5 seconds,
    #  we should somehow optimize this
    app_info = AppInfo.objects.filter(station_id=station_id).order_by('-date')[:1]

    if app_info:
        app_info = app_info[0]
        res = {
            "station": station_id,
            "version": app_info.version,
            "versionCode": app_info.version_code,
            "online": app_info.online,
            "date": app_info.date,
        }
    else:
        res = None

    return str(station_id), res

def map_app_info(app_infos: List[AppInfo]):
    tuples = []
    for app_info in app_infos:
        # if app_info.station_id == station_id:
        res = {
            "station": app_info.station_id,
            "version": app_info.version,
            "versionCode": app_info.version_code,
            "online": app_info.online,
            "date": app_info.date,
        }
        tuples.append((str(app_info.station_id), res))

    return iter(tuples)
    # return str(station_id), None


class StationInfoListApiView(APIView):
    permission_classes = [IsAuthenticated]
    authentication_classes = [JWTStatelessUserAuthentication]

    def get(self, request, *args, **kwargs):
        stations = Station.objects.all()

        latest_per_station = list(
            AppInfo.objects
            .annotate(
                row_number=Window(
                    expression=RowNumber(),
                    partition_by=[F('station_id')],
                    order_by=F('date').desc()
                )
            )
            .filter(row_number=1)
        )

        start = time.time()
        result = dict(map_app_info(list(latest_per_station)))
        # result = dict(get_app_info(station.id) for station in stations)
        end = time.time()

        logger.error("StationInfoListApiView took {} seconds.".format(end - start))

        return Response(result, status=status.HTTP_200_OK)


class UserListApiView(APIView):
    def get(self, request, *args, **kwargs):
        users = User.objects.all()
        serializer = UserSerializer(users, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class CrewListApiView(APIView):
    permission_classes = [IsAuthenticated]
    authentication_classes = [JWTStatelessUserAuthentication]

    def get(self, request, date, *args, **kwargs):
        crews = CrewInfo.objects.filter(date=date)
        serializer = CrewInfoSerializer(crews, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class EntryListApiView(APIView):
    permission_classes = [IsAuthenticated]
    authentication_classes = [JWTStatelessUserAuthentication]

    def get(self, request, date, *args, **kwargs):
        date = datetime.strptime(date, '%Y-%m-%d').date()
        entries = Entry.objects.filter(date__date=date)
        serializer = EntrySerializer(entries, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class EventInfoApiView(APIView):
    permission_classes = [IsAuthenticated]
    authentication_classes = [JWTStatelessUserAuthentication]

    def get(self, request, date, *args, **kwargs):
        date = datetime.strptime(date, '%Y-%m-%d').date()
        events = Event.objects.filter(date__date=date)

        result = reduce(reduce_events, events, {'firstAid': 0, 'search': 0})

        result = {
            "date": date,
            **result,
        }

        return Response(result, status=status.HTTP_200_OK)


# todo: move business logic somewhere else
def reduce_events(prev: dict, cur: Event) -> dict:
    if cur.type == Event.EventType.FIRST_AID:
        prev['firstAid'] = prev['firstAid'] + 1
    if cur.type == Event.EventType.SEARCH:
        prev['search'] = prev['search'] + 1

    return prev


class SpecialEventListApiView(APIView):
    permission_classes = [IsAuthenticated]
    authentication_classes = [JWTStatelessUserAuthentication]

    def get(self, request, date, *args, **kwargs):
        date = datetime.strptime(date, '%Y-%m-%d').date()
        events = SpecialEvent.objects.filter(date__date=date)
        serializer = SpecialEventSerializer(events, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class FieldListApiView(APIView):
    permission_classes = [IsAuthenticated]
    authentication_classes = [JWTStatelessUserAuthentication]

    def get(self, request, *args, **kwargs):
        fields = Field.objects.all()
        serializer = FieldSerializer(fields, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)


class FieldDetailApiView(APIView):
    permission_classes = [IsAuthenticated]
    authentication_classes = [JWTStatelessUserAuthentication]

    def get(self, request, id, *args, **kwargs):
        field = Field.objects.get(id=id)
        serializer = FieldSerializer(field)
        return Response(serializer.data, status=status.HTTP_200_OK)


class MeApiView(APIView):
    permission_classes = [IsAuthenticated]
    authentication_classes = [JWTStatelessUserAuthentication]

    def get(self, request, *args, **kwargs):
        # todo: use token
        user_id = request.user.id
        user = User.objects.get(id=user_id)
        serializer = UserLoggedInSerializer(user)
        return Response(serializer.data, status=status.HTTP_200_OK)


class ProvisionCreateApiView(APIView):
    permission_classes = [IsAdminUser]
    authentication_classes = [JWTStatelessUserAuthentication]

    def post(self, request, station_id, *args, **kwargs):
        station = Station.objects.get(id=station_id)

        password = generate_password(12)
        expiration = datetime.now() + timedelta(days=1)

        request = StationProvisioningRequest.objects.create(
            station_id=station.id, password=password,
            expires_at=expiration, active=True
        )
        serializer = StationProvisioningRequestSerializer(request)
        return Response(serializer.data, status=status.HTTP_200_OK)


# todo: move somewhere else
def generate_password(length: int = 12) -> str:
    if length < 1:
        raise ValueError("Password length must be at least 1")

    keyspace = string.ascii_letters + string.digits + "_-/"
    return ''.join(secrets.choice(keyspace) for _ in range(length))


# todo: move somewhere else
class ProvisionBasicAuthentication(authentication.BasicAuthentication):
    def authenticate(self, request):
        if request.method != 'POST':
            return None

        # require basic auth
        auth = get_authorization_header(request).split()

        if not auth or auth[0].lower() != b'basic':
            raise exceptions.AuthenticationFailed('No authorization header provided.')

        return super(ProvisionBasicAuthentication, self).authenticate(request)

    def authenticate_credentials(self, userid, password, request=None):
        """
        Authenticate the userid and password against username and password
        with optional request for context.
        """
        if userid != 'beachist':
            raise exceptions.AuthenticationFailed

        try:
            provisioning_request = StationProvisioningRequest.objects.get(password=password)
        except StationProvisioningRequest.DoesNotExist:
            raise exceptions.AuthenticationFailed

        if not provisioning_request:
            raise exceptions.AuthenticationFailed

        if provisioning_request.expires_at < datetime.now(timezone.utc):
            raise exceptions.AuthenticationFailed

        if not provisioning_request.active:
            raise exceptions.AuthenticationFailed

        user = ProvisioningUser()
        user.station_id = provisioning_request.station_id

        # todo: this was used so we disable it

        return user, None


class ProvisionListApiView(APIView):
    authentication_classes = [
        # this only guards the POST request
        ProvisionBasicAuthentication,
        JWTStatelessUserAuthentication,
    ]

    def get(self, request, *args, **kwargs):
        if not request.user.is_staff:
            raise exceptions.PermissionDenied

        date = datetime.now()
        provisionings = StationProvisioningRequest.objects.filter(active=True, expires_at__gte=date)
        serializer = StationProvisioningRequestSerializer(provisionings, many=True)
        return Response(serializer.data, status=status.HTTP_200_OK)

    def post(self, request, *args, **kwargs):
        station_id = request.user.station_id

        if not station_id:
            raise exceptions.AuthenticationFailed

        client_private_key, client_public_key, client_cert, client_cert_id = generate_client_cert(station_id)

        response = {
            'privateKey': client_private_key,
            'publicKey': client_public_key,
            'certificatePem': client_cert,
            'certificateId': client_cert_id,
            'thingName': station_id,
            'dataEndpoint': 'eu-central-1.192.168.23.170',
            'credentialsEndpoint': '...',
        }

        return Response(response, status=status.HTTP_200_OK)

class MqttMessageListView(APIView):
    authentication_classes = [JWTStatelessUserAuthentication]
    permission_classes = [IsAdminUser]
    page_size = 100

    def get(self, request, *args, **kwargs):
        items = MqttMessage.objects.all()

        paginator = CursorPagination()
        paginator.page_size = self.page_size
        paginator.ordering = '-ts'
        result_page = paginator.paginate_queryset(items, request)
        serializer = MqttMessageSerializer(result_page, many=True)
        return paginator.get_paginated_response(serializer.data)
