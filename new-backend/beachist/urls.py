"""
URL configuration for beachist project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/5.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from collections import UserList

from django.urls import path
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView

from .views import ProvisionCreateApiView, CrewListApiView, EntryListApiView, EventInfoApiView, FieldListApiView, \
    SpecialEventListApiView, \
    StationListApiView, StationInfoListApiView, StationDetailApiView, UserListApiView, FieldDetailApiView, MeApiView, \
    ProvisionListApiView, MqttMessageListView

urlpatterns = [
    path('api/station', StationListApiView.as_view()),
    path('api/station/info', StationInfoListApiView.as_view()),
    path('api/station/<uuid:id>', StationDetailApiView.as_view()),
    path('auth/users', UserListApiView.as_view()),
    # path('auth/link', ...),
    path('api/crew/<date>', CrewListApiView.as_view()),
    path('api/entry/<date>', EntryListApiView.as_view()),
    path('api/event/<date>', EventInfoApiView.as_view()),
    path('api/special/<date>', SpecialEventListApiView.as_view()),
    path('api/field', FieldListApiView.as_view()),
    path('api/field/<uuid:id>', FieldDetailApiView.as_view()),
    path('api/me', MeApiView.as_view()),
    path('api/provision/<uuid:station_id>', ProvisionCreateApiView.as_view()),
    path('api/provision', ProvisionListApiView.as_view()),
    path('auth/login', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('auth/refresh', TokenRefreshView.as_view(), name='token_refresh'),
    path('api/mqtt', MqttMessageListView.as_view()),
]
