package de.tjarksaul.wachmanager.iot

import androidx.lifecycle.LiveData

interface IotRepository {
    fun observeConnection(): LiveData<ConnectionState>
    fun connect(config: de.tjarksaul.wachmanager.iotClient.IotConfig)
    fun disconnect()
}

sealed class ConnectionState {
    object Connected : ConnectionState()
    object Disconnected : ConnectionState()
}