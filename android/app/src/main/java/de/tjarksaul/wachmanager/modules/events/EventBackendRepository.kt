package de.tjarksaul.wachmanager.modules.events

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import de.tjarksaul.wachmanager.dtos.EventType
import app.beachist.iot_client.client.IotClient
import app.beachist.iot_client.client.IotConnectionState
import app.beachist.auth.station.StationNameProvider
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class EventBackendRepository(
    private val iotClient: IotClient,
    private val stationNameProvider: StationNameProvider,
    private val gson: Gson,
) {
    private val disposables = CompositeDisposable()
    private val subject: BehaviorSubject<String> = BehaviorSubject.create()

    fun observeEventUpdates(): Observable<String> = subject

    init {
        subscribe()
    }

    fun createEvent(type: EventType, id: String, date: String) {
        Handler(Looper.getMainLooper()).post {
            val disposable = CompositeDisposable()
            disposable += iotClient.getConnectionState().subscribe {
                    if (it == IotConnectionState.Connected) {
                        val event = PostEvent(type, id, date)
                        val stationName = stationNameProvider.currentStationName()
                        // todo: only publish once here
                        // notes for future me: use two observables and combine latest
                        iotClient.publish("${stationName}/event", gson.toJson(event))
                        disposable.dispose()
                    }
                }
        }
    }

    private fun subscribe() {
        disposables += iotClient.getConnectionState().subscribe {
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
