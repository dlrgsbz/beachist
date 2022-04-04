package de.tjarksaul.wachmanager.modules.stationCheck

import com.google.gson.Gson
import de.tjarksaul.wachmanager.dtos.Field
import de.tjarksaul.wachmanager.iotClient.IotClient
import de.tjarksaul.wachmanager.service.StationNameProvider
import io.reactivex.Observable
import timber.log.Timber
import com.google.gson.reflect.TypeToken

class FieldService(
    private val iotClient: IotClient,
    private val stationNameProvider: StationNameProvider,
    private val gson: Gson
) {
    val fieldsObservable: Observable<List<Field>>

    init {
        val listType = object : TypeToken<ArrayList<Field>>() {}.type

        fieldsObservable = Observable.create {
            iotClient.subscribe("fields/${stationNameProvider.currentStationName()}", { status ->
                // todo: reject observable on error
                Timber.d("Connected with $status")
            }, { value ->
                Timber.tag("FieldService").d(value)
                it.onNext(gson.fromJson(value, listType))
            })
        }
    }

    fun getFieldsWithEntries() {
        val stationName = stationNameProvider.currentStationName()
        iotClient.publish("${stationName}/field/get", "")
    }
}
