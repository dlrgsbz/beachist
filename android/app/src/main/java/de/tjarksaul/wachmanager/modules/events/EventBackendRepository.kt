package de.tjarksaul.wachmanager.modules.events

import com.google.gson.Gson
import de.tjarksaul.wachmanager.dtos.EventType
import de.tjarksaul.wachmanager.iotClient.IotClient
import de.tjarksaul.wachmanager.service.StationNameProvider
import io.reactivex.Observable
import timber.log.Timber

class EventBackendRepository(
    private val iotClient: IotClient,
    private val stationNameProvider: StationNameProvider,
    private val gson: Gson
) {
    fun createEvent(type: EventType, id: String, date: String) {
        val event = PostEvent(type, id, date)
        val stationName = stationNameProvider.currentStationName()
        iotClient.publish("${stationName}/event", gson.toJson(event))
    }

    fun observeEventUpdates(): Observable<String> {
        return Observable.create {
            iotClient.subscribe("events/${stationNameProvider.currentStationName()}", { status ->
                // todo: reject observable on error
                Timber.d("Connected with $status")
            }, { value ->
                it.onNext(value)
            })
        }
    }
}
