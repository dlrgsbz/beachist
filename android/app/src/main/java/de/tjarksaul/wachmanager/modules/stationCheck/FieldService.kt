package de.tjarksaul.wachmanager.modules.stationCheck

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.tjarksaul.wachmanager.dtos.Field
import de.tjarksaul.wachmanager.iotClient.IotClient
import de.tjarksaul.wachmanager.iotClient.IotConnectionState
import de.tjarksaul.wachmanager.service.StationNameProvider
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class FieldService(
    private val iotClient: IotClient,
    private val stationNameProvider: StationNameProvider,
    private val gson: Gson,
) {
    private val subject: BehaviorSubject<List<Field>> = BehaviorSubject.createDefault(listOf())
    val fieldsObservable: Observable<List<Field>> = subject

    init {
        subscribe()
    }

    private fun subscribe() {
        val listType = object : TypeToken<ArrayList<Field>>() {}.type

        iotClient.getConnectionState().observeForever {
            when (it) {
                is IotConnectionState.Connected -> {
                    iotClient.subscribe("fields/${stationNameProvider.currentStationName()}",
                        { status ->
                            // todo: reject observable on error
                            Timber.d("Connected to fields/ with $status")
                        },
                        { value ->
                            Timber.tag("FieldService").d(value)
                            subject.onNext(gson.fromJson(value, listType))
                        })

                }
                is IotConnectionState.ConnectionLost -> {
                    iotClient.unsubscribe(
                        "fields/${stationNameProvider.currentStationName()}",
                    ) { status ->
                        Timber.d("Disconnected from fields/ with $status")
                    }
                }
                else -> {
                    // Ignoring this on purpose
                }
            }
        }
    }

    fun getFieldsWithEntries() {
        iotClient.getConnectionState().observeForever {
            if (it == IotConnectionState.Connected) {
                val stationName = stationNameProvider.currentStationName()
                iotClient.publish("${stationName}/field/get", "")
            }
        }
    }
}
