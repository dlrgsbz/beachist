package de.tjarksaul.wachmanager.modules.crew.api

import com.google.gson.Gson
import de.tjarksaul.wachmanager.iotClient.IotClient
import de.tjarksaul.wachmanager.iotClient.IotConnectionState
import de.tjarksaul.wachmanager.modules.crew.repository.CrewRepository
import de.tjarksaul.wachmanager.service.StationNameProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi()
class CrewApiImpl(
    private val iotClient: IotClient,
    private val crewRepository: CrewRepository,
    private val stationNameProvider: StationNameProvider,
    private val gson: Gson,
): CoroutineScope {
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + parentJob

    private val disposables = CompositeDisposable()

    init {
        disposables += iotClient.getConnectionState().subscribe {
            if (it == IotConnectionState.Connected) {
                syncCrewInfo()
            }
        }
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
