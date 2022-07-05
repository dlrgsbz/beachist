package de.tjarksaul.wachmanager.modules.specialEvents.sync

import com.google.gson.Gson
import app.beachist.shared.NetworkState
import de.tjarksaul.wachmanager.dtos.SpecialEvent
import app.beachist.iot_client.client.IotClient
import app.beachist.iot_client.client.IotConnectionState
import de.tjarksaul.wachmanager.modules.specialEvents.repository.SpecialEventRepository
import app.beachist.shared.date.DateFormatProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class SyncSpecialEvents(
    private val repository: SpecialEventRepository,
    private val iotClient: IotClient,
    private val stationNameProvider: app.beachist.auth.station.StationNameProvider,
    private val formatProvider: DateFormatProvider,
    private val gson: Gson,
) : CoroutineScope {
    private val disposable = CompositeDisposable()
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + parentJob

    private val previousTopics: MutableList<String> = mutableListOf()

    init {
        syncEvents()
        subscribeToSuccesses()
    }

    private fun syncEvents() {
        repository.getSpecialEvents(Date()).mapLatest {
            val syncableEvents = it.filter { event ->
                return@filter event.networkState == NetworkState.pending || event.networkState == NetworkState.failed
            }

            Timber.tag("SyncSpecialEvents").i("Found ${syncableEvents.count()} events to sync")
            syncableEvents.forEach { ev ->
                syncEvent(ev)
            }
        }.launchIn(this)
    }

    private fun subscribeToSuccesses() {
        disposable += iotClient.getConnectionState().subscribe { connectionState ->
            if (connectionState != IotConnectionState.Connected) {
                unsubscribeFromPreviousTopics()
                return@subscribe
            }

            stationNameProvider.stationNameFlow.mapLatest {
                unsubscribeFromPreviousTopics()

                val stationName = it ?: return@mapLatest
                val topic = "special-event/$stationName/success"
                iotClient.subscribe(topic, { status ->
                    Timber.tag("SyncSpecialEvents").i("Subscribed to $topic with status $status")
                }) { id ->
                    onSpecialEventResponse(id)
                }
            }.launchIn(this)
        }
    }

    private fun unsubscribeFromPreviousTopics() {
        previousTopics.forEach { topic ->
            iotClient.unsubscribe(topic) { status ->
                Timber.tag("SyncSpecialEvents").i("Unsubscribe from $topic with status $status")
            }
        }
    }

    private fun syncEvent(event: SpecialEvent) {
        Timber.tag("SyncSpecialEvents").i("Publishing event ${event.id}")
        val stationName = stationNameProvider.currentStationName()
        iotClient.publish(
            "$stationName/special-event",
            gson.toJson(event.toSyncSpecialEvent(formatProvider)),
        )
    }

    private fun onSpecialEventResponse(id: String) {
        launch {
            repository.updateNetworkState(id, NetworkState.successful)
        }
    }
}

private fun SpecialEvent.toSyncSpecialEvent(formatProvider: DateFormatProvider): SyncSpecialEvent {
    val date = formatProvider.getIso8601DateTimeFormat().format(this.date)
    return SyncSpecialEvent(
        id,
        title,
        note,
        notifier,
        date,
        kind,
    )
}