package app.beachist.event.sync

import app.beachist.auth.station.StationNameProvider
import app.beachist.event.dto.Event
import app.beachist.event.repository.EventRepository
import app.beachist.iot_client.client.IotClient
import app.beachist.iot_client.client.IotConnectionState
import app.beachist.shared.NetworkState
import app.beachist.shared.date.DateFormatProvider
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class SyncEvents(
    private val repository: EventRepository,
    private val iotClient: IotClient,
    private val stationNameProvider: StationNameProvider,
    private val formatProvider: DateFormatProvider,
    private val gson: Gson,
) : CoroutineScope {
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + parentJob

    private val previousTopics: MutableList<String> = mutableListOf()

    init {
        syncEvents()
        subscribeToSuccesses()
    }

    private fun syncEvents() {
        repository.observeEvents().mapLatest {
            val syncableEvents = it.filter { event ->
                return@filter event.state == NetworkState.pending || event.state == NetworkState.failed
            }

            Timber.tag("SyncEvents").i("Found ${syncableEvents.count()} events to sync")
            syncableEvents.forEach { ev ->
                syncEvent(ev)
            }
        }.launchIn(this)
    }

    @Suppress("NAME_SHADOWING")
    private fun subscribeToSuccesses() {
        iotClient.observeConnectionState()
            .combine(stationNameProvider.stationNameFlow) { connection, stationName ->
                if (connection != IotConnectionState.Connected) {
                    return@combine ConnectionInfo.Disconnected
                }

                val stationName = stationName ?: return@combine ConnectionInfo.Disconnected
                return@combine ConnectionInfo.Connected(stationName)
            }
            .mapLatest {
                unsubscribeFromPreviousTopics()
                return@mapLatest it
            }
            .filterIsInstance<ConnectionInfo.Connected>()
            .mapLatest {
                val stationName = it.stationName
                val topic = "events/$stationName"
                iotClient.subscribe(topic, { status ->
                    Timber.tag("SyncEvents").i("Subscribed to $topic with status $status")
                }) { id ->
                    onEventResponse(id)
                }
            }
            .launchIn(this)
    }


    private fun unsubscribeFromPreviousTopics() {
        previousTopics.forEach { topic ->
            iotClient.unsubscribe(topic) { status ->
                Timber.tag("SyncEvents").i("Unsubscribe from $topic with status $status")
            }
        }
    }

    private fun syncEvent(event: Event) {
        Timber.tag("SyncEvents").i("Publishing event ${event.id}")
        val stationName = stationNameProvider.currentStationName()
        iotClient.publish(
            "$stationName/event",
            gson.toJson(event.toSyncEvent(formatProvider)),
        )
    }

    private fun onEventResponse(id: String) {
        launch {
            repository.updateNetworkState(id, NetworkState.successful)
        }
    }
}

private fun Event.toSyncEvent(formatProvider: DateFormatProvider): SyncEvent {
    val date = formatProvider.getIso8601DateTimeFormat().format(this.date)
    return SyncEvent(
        type,
        id,
        date,
    )
}

internal sealed class ConnectionInfo {
    object Disconnected : ConnectionInfo()
    data class Connected(val stationName: String) : ConnectionInfo()
}
