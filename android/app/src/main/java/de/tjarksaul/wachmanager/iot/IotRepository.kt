package de.tjarksaul.wachmanager.iot

import io.reactivex.Observable

interface IotRepository {
    fun observeConnection(): Observable<ConnectionState>
    fun connect(config: de.tjarksaul.wachmanager.iotClient.IotConfig)
    fun disconnect()
}

sealed class ConnectionState {
    object Connected : ConnectionState()
    object Disconnected : ConnectionState()
}