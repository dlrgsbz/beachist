package de.tjarksaul.wachmanager.iot

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.gson.Gson
import de.tjarksaul.wachmanager.iotClient.*
import de.tjarksaul.wachmanager.modules.shared.AppVersionRepository
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class IotRepositoryImpl(
    private val gson: Gson,
    private val iotClient: IotClient,
    private val versionRepo: AppVersionRepository
) : IotRepository,
    // todo: move this somewhere
    CoroutineScope {

    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + parentJob

    private val connection: LiveData<ConnectionState> =
        Transformations.map(iotClient.getConnectionState()) { connectionState ->
            when (connectionState) {
                IotConnectionState.Connected -> ConnectionState.Connected
                else -> ConnectionState.Disconnected
            }
        }

    private var config: IotConfig? = null
    override fun observeConnection(): LiveData<ConnectionState> = connection

    override fun connect(config: IotConfig) {
        if (connection.value is ConnectionState.Connected) {
            return
        }
        this.config = config

        iotClient.connect(config)

        launch(coroutineContext) {
            while (iotClient.peekConnectionState() == null || iotClient.peekConnectionState() != IotConnectionState.Connected) {
                Timber.tag("IotRepository").d("waiting for connection....")
                delay(3 * 1_000L)
            }
            Timber.tag("IotRepository").i("updating shadow")
            iotClient.publish(
                "\$aws/things/${config.clientId}/shadow/update",
                ShadowData(
                    connected = true,
                    appVersion = versionRepo.getAppVersionName(),
                    appVersionCode = versionRepo.getAppVersionCode(),
                ).toJson(gson = gson)
            )
        }
    }

    override fun disconnect() {
        config?.let {
            iotClient.publish(
                "\$aws/things/${it.clientId}/shadow/update",
                gson.toJson(ShadowData(connected = false))
            )
        }

        iotClient.disconnect()
    }
}