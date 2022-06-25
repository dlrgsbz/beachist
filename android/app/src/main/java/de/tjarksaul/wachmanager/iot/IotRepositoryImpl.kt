package de.tjarksaul.wachmanager.iot

import com.google.gson.Gson
import de.tjarksaul.wachmanager.iotClient.IotClient
import de.tjarksaul.wachmanager.iotClient.IotConfig
import de.tjarksaul.wachmanager.iotClient.IotConnectionState
import de.tjarksaul.wachmanager.iotClient.ShadowData
import de.tjarksaul.wachmanager.iotClient.toJson
import de.tjarksaul.wachmanager.modules.shared.AppVersionRepository
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class IotRepositoryImpl(
    private val gson: Gson,
    private val iotClient: IotClient,
    private val versionRepo: AppVersionRepository,
) : IotRepository,
    // todo: move this somewhere
    CoroutineScope {

    private val disposable = CompositeDisposable()
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + parentJob

    private val connection: BehaviorSubject<ConnectionState> =
        BehaviorSubject.createDefault(ConnectionState.Disconnected)

    init {
        disposable += iotClient.getConnectionState().subscribe { connectionState ->
            connection.onNext(
                when (connectionState) {
                    IotConnectionState.Connected -> ConnectionState.Connected
                    else -> ConnectionState.Disconnected
                }
            )
        }
    }

    private var config: IotConfig? = null
    override fun observeConnection(): Observable<ConnectionState> = connection

    override fun connect(config: IotConfig) {
        if (connection.value is ConnectionState.Connected) {
            return
        }
        this.config = config

        disposable += connection.subscribe {
            Timber.tag("IotRepository").i("Connection state changed to $it")
            if (it == ConnectionState.Connected) {
                launch(coroutineContext) {
                    // we want to make sure that the LWT gets processed first
                    delay(1_000L)
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
        }

        iotClient.connect(config)
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