package app.beachist.iot_client.types

import app.beachist.shared.date.DateFormatProvider
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.*

class DateTypeAdapter(dateFormatProvider: DateFormatProvider): TypeAdapter<Date?>() {
    private val formatter = dateFormatProvider.getIso8601DateTimeFormat()
    override fun write(out: JsonWriter?, value: Date?) {
        if (value === null) {
            out?.nullValue()
            return
        }
        val formatted = formatter.format(value)
        out?.value(formatted)
    }

    override fun read(reader: JsonReader?): Date? {
        val string = reader?.nextString() ?: return null
        return formatter.parse(string)
    }
}
