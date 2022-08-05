package app.beachist.iot.repository

import io.reactivex.Observable

interface IotRepository {
    fun observeConnection(): Observable<ConnectionState>
    fun connect(config: app.beachist.iot_client.client.IotConfig)
    fun disconnect()
}

sealed class ConnectionState {
    object Connected : ConnectionState()
    object Disconnected : ConnectionState()
}