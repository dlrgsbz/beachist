package app.beachist.iot_client

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import app.beachist.iot_client.types.DateTypeAdapter
import app.beachist.shared.date.DateFormatProvider
import java.util.Date

fun buildGson(dateFormatProvider: DateFormatProvider): Gson = GsonBuilder()
    .registerTypeAdapter(Date::class.java, DateTypeAdapter(dateFormatProvider))
    .create()
