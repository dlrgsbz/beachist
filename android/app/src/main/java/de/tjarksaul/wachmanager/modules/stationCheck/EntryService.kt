package de.tjarksaul.wachmanager.modules.stationCheck

import com.google.gson.Gson
import de.tjarksaul.wachmanager.dtos.PostEntry
import de.tjarksaul.wachmanager.dtos.StateKind
import app.beachist.iot_client.client.IotClient
import de.tjarksaul.wachmanager.service.StationNameProvider

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
