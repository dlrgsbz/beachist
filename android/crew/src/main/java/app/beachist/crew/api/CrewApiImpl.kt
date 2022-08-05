package app.beachist.crew.api

import app.beachist.auth.station.StationNameProvider
import app.beachist.crew.repository.CrewRepository
import app.beachist.iot_client.client.IotClient
import app.beachist.iot_client.client.IotConnectionState
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class CrewApiImpl(
    private val iotClient: IotClient,
    private val crewRepository: CrewRepository,
    private val stationNameProvider: StationNameProvider,
    private val gson: Gson,
) : CoroutineScope {
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + parentJob

    init {
        iotClient.observeConnectionState()
            .onEach {
                if (it == IotConnectionState.Connected) {
                    syncCrewInfo()
                }
            }
            .launchIn(this)
    }

    private fun syncCrewInfo() {
        Timber.i("Starting crew info sync")
        crewRepository.getCrewInfo()
            .filterNotNull()
            .mapLatest {
                val stationName = stationNameProvider.currentStationName()

                Timber.tag("CrewApiImpl").d("Publishing $it to $stationName/crew")

                iotClient.publish("$stationName/crew", gson.toJson(it))
            }
            .launchIn(this)
    }
}
