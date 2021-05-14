package de.tjarksaul.wachmanager.api

import de.tjarksaul.wachmanager.dtos.*
import de.tjarksaul.wachmanager.dtos.Field
import de.tjarksaul.wachmanager.ui.events.PostEvent
import retrofit2.Call
import retrofit2.http.*
import io.reactivex.Observable

interface WachmanagerService {
    @GET("station")
    fun getStations(): Call<MutableList<Station>>

    @GET("station/{stationId}/field")
    fun getFields(@Path("stationId") stationId: String): Call<MutableList<Field>>

    @GET("entry/{date}/{stationId}")
    fun getEntries(@Path("date") date: String, @Path("stationId") stationId: String): Call<MutableList<Entry>>

    @POST("station/{stationId}/field/{fieldId}/entry")
    fun updateEntries(@Path("stationId") stationId: String, @Path("fieldId") fieldId: String, @Body entry: PostEntry): Call<IdResponse>

    @GET("event/{date}/{stationId}")
    fun getEvents(@Path("date") date: String, @Path("stationId") stationId: String): Call<EventStats>
    @POST("station/{stationId}/event")
    fun createEvent(@Path("stationId") stationId: String, @Body info: PostEvent): Observable<IdResponse>

    @POST("station/{stationId}/special")
    fun createSpecialEvent(@Path("stationId") stationId: String, @Body event: PostSpecialEvent): Call<IdResponse>
}
