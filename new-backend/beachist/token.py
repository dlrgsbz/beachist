from rest_framework_simplejwt.serializers import TokenObtainPairSerializer

from .models import User


class BeachistTokenObtainPairSerializer(TokenObtainPairSerializer):
    @classmethod
    def get_token(cls, user: User):
        token = super().get_token(user)

        token['username'] = user.name
        token['description'] = user.description
        token['permissions'] = user.roles
        token['is_staff'] = user.is_staff

        return token
