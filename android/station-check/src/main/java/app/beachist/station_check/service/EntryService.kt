package app.beachist.station_check.service

import com.google.gson.Gson
import app.beachist.iot_client.client.IotClient
import app.beachist.auth.station.StationNameProvider
import app.beachist.station_check.dtos.PostEntry
import app.beachist.station_check.dtos.StateKind

class EntryService(
    private val iotClient: IotClient,
    private val stationNameProvider: StationNameProvider,
    private val gson: Gson
) {
    fun updateEntry(
        fieldId: String,
        state: Boolean,
        stateKind: StateKind?,
        amount: Int?,
        note: String?
    ) {
        val entry = PostEntry(state, stateKind, amount, note)
        val stationName = stationNameProvider.currentStationName()
        iotClient.publish("${stationName}/field/${fieldId}/entry", gson.toJson(entry))
    }
}
