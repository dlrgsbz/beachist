package de.tjarksaul.wachmanager.modules.events

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.gson.Gson
import de.tjarksaul.wachmanager.dtos.EventType
import de.tjarksaul.wachmanager.iotClient.IotClient
import de.tjarksaul.wachmanager.iotClient.IotConnectionState
import de.tjarksaul.wachmanager.service.StationNameProvider
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class EventBackendRepository(
    private val iotClient: IotClient,
    private val stationNameProvider: StationNameProvider,
    private val gson: Gson,
) {
    private val subject: BehaviorSubject<String> = BehaviorSubject.create()

    fun observeEventUpdates(): Observable<String> = subject

    init {
        subscribe()
    }

    fun createEvent(type: EventType, id: String, date: String) {
        Handler(Looper.getMainLooper()).post {
            iotClient.getConnectionState().observeForever(object : Observer<IotConnectionState> {
                override fun onChanged(it: IotConnectionState) {
                    if (it == IotConnectionState.Connected) {
                        val event = PostEvent(type, id, date)
                        val stationName = stationNameProvider.currentStationName()
                        // todo: only publish once here
                        // notes for future me: use two observables and combine latest
                        iotClient.publish("${stationName}/event", gson.toJson(event))
                        iotClient.getConnectionState().removeObserver(this)
                    }
                }
            })
        }
    }

    private fun subscribe() {
        iotClient.getConnectionState().observeForever {
            when (it) {
                is IotConnectionState.Connected -> {
                    iotClient.subscribe("events/${stationNameProvider.currentStationName()}",
                        { status ->
                            // todo: reject observable on error
                            Timber.d("Connected to events/ with $status")
                        },
                        { value ->
                            subject.onNext(value)
                        })
                }
                is IotConnectionState.ConnectionLost -> {
                    iotClient.unsubscribe(
                        "events/${stationNameProvider.currentStationName()}",
                    ) { status ->
                        Timber.d("Disconnected from events/ with $status")
                    }
                }
                else -> {
                    // Ignoring this on purpose
                }
            }
        }
    }
}
