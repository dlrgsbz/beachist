package app.beachist.station_check.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import app.beachist.iot_client.client.IotClient
import app.beachist.iot_client.client.IotConnectionState
import app.beachist.auth.station.StationNameProvider
import app.beachist.station_check.dtos.Field
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class FieldService(
    private val iotClient: IotClient,
    private val stationNameProvider: StationNameProvider,
    private val gson: Gson,
) {
    private val disposables = CompositeDisposable()

    private val subject: BehaviorSubject<List<Field>> = BehaviorSubject.createDefault(listOf())
    val fieldsObservable: Observable<List<Field>> = subject

    init {
        subscribe()
    }

    private fun subscribe() {
        val listType = object : TypeToken<ArrayList<Field>>() {}.type

        disposables += iotClient.getConnectionState().subscribe {
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
        disposables += iotClient.getConnectionState().subscribe {
            if (it == IotConnectionState.Connected) {
                val stationName = stationNameProvider.currentStationName()
                iotClient.publish("${stationName}/field/get", "")
            }
        }
    }
}
