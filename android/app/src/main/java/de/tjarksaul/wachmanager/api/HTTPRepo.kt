package de.tjarksaul.wachmanager.api

import de.tjarksaul.wachmanager.BuildConfig
import de.tjarksaul.wachmanager.config.backendUrl
import de.tjarksaul.wachmanager.dtos.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class HTTPRepo {
    private val service: WachmanagerService

    companion object {
        const val BASE_URL = backendUrl
    }

    init {
        val defaultHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request: Request = chain.request().newBuilder()
                    .addHeader("X-Wachmanager-Version", BuildConfig.VERSION_NAME).build()
                chain.proceed(request)
            }.build()

        // 2
        val retrofit = Retrofit.Builder()
            // 1
            .baseUrl(BASE_URL)
            //3
            .addConverterFactory(GsonConverterFactory.create())
            .client(defaultHttpClient)
            .build()
        //4
        service = retrofit.create(WachmanagerService::class.java)
    }

    fun getFields(stationId: String, callback: Callback<MutableList<Field>>) { //5
        val call = service.getFields(stationId)
        call.enqueue(callback)
    }

    fun getEntries(stationId: String, callback: Callback<MutableList<Entry>>) {
        val date = Calendar.getInstance().time
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val strDate: String = dateFormat.format(date)

        val call = service.getEntries(strDate, stationId)
        call.enqueue(callback)
    }

    fun updateEntry(
        fieldId: String,
        stationId: String,
        state: Boolean,
        stateKind: StateKind?,
        amount: Int?,
        note: String?,
        crew: String,
        callback: Callback<IdResponse>
    ) {
        val entry = PostEntry(state, stateKind, amount, note, crew)
        val call = service.updateEntries(stationId, fieldId, entry)
        call.enqueue(callback)
    }

    fun getEvents(stationId: String, callback: Callback<EventStats>) {
        val date = Calendar.getInstance().time
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val strDate: String = dateFormat.format(date)

        val call = service.getEvents(strDate, stationId)
        call.enqueue(callback)
    }

    fun createEvent(stationId: String, type: EventType, callback: Callback<IdResponse>) {
        val event = PostEvent(type)

        val call = service.createEvent(stationId, event)
        call.enqueue(callback)
    }

    fun getStations(callback: Callback<MutableList<Station>>) {
        val call = service.getStations()
        call.enqueue(callback)
    }

    fun createSpecialEvent(
        stationId: String,
        title: String,
        note: String,
        notifier: String,
        type: SpecialEventKind,
        callback: RequestCallback<IdResponse>
    ) {
        val event = PostSpecialEvent(title, note, notifier, type)

        val call = service.createSpecialEvent(stationId, event)
        RetryingRequest(call, callback)
    }
}