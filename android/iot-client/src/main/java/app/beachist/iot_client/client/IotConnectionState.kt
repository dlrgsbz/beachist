package app.beachist.iot_client.client

import timber.log.Timber

sealed class IotConnectionState {
    object Initial : IotConnectionState()
    object Connected : IotConnectionState()
    class ConnectionLost(val message: String? = null) : IotConnectionState() {

        companion object {
            fun from(error: Throwable?): ConnectionLost {
                Timber.tag("ConnectionLost").w("Connection lost")
                return ConnectionLost(error?.toString())
            }
        }
    }
}
