package app.beachist.iot_client.client

import com.google.gson.Gson
import com.google.gson.JsonObject

data class Shadow(
    val state: ShadowState,
    val version: Int? = null,
    val clientToken: String? = null
)

data class ShadowState(
    val reported: ShadowData? = null,
    val desired: ShadowData? = null,
    val delta: ShadowData? = null
)

data class ShadowData(
    val connected: Boolean? = null,
    val stationId: String? = null,
    val appVersion: String? = null,
    val appVersionCode: Long? = null
)

fun ShadowData.toJson(desired: JsonObject? = null, gson: Gson): String {
    val metaState = Shadow(
        state = ShadowState(
            reported = ShadowData(
                connected = connected,
                stationId = stationId,
                appVersion = appVersion,
                appVersionCode = appVersionCode
            )
        )
    )
    val json = gson.toJsonTree(metaState).asJsonObject

    desired?.let {
        json.getAsJsonObject("state").add("desired", desired)
    }

    return json.toString()
}