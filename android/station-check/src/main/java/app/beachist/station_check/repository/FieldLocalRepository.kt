package app.beachist.station_check.repository

import android.content.Context
import app.beachist.station_check.dtos.Field
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.IOException


class FieldLocalRepository(
    private val gson: Gson,
    private val context: Context
) {
    fun getCachedFields(): List<Field> {
        lateinit var jsonString: String
        try {
            jsonString = context.assets.open("fields.json")
                .bufferedReader()
                .use { it.readText() }
        } catch (ioException: IOException) {
            Timber.tag("FieldLocalRepository").d(ioException)
        }

        val fieldListTypeToken = object : TypeToken<List<Field>>() {}.type
        return gson.fromJson(jsonString, fieldListTypeToken)
    }
}
