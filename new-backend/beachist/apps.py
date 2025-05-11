from django.apps import AppConfig


class BeachistAppConfig(AppConfig):
    default_auto_field = "django.db.models.BigAutoField"
    name = "beachist"

    def ready(self):
        from . import mqtt
        # Ensures the client starts with Django
        client = mqtt.MqttClient()
        client.start()
