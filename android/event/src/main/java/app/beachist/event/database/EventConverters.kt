package app.beachist.event.database

import androidx.room.TypeConverter
import app.beachist.event.dto.EventType
import app.beachist.shared.NetworkState
import java.util.*

internal class EventConverters {
    @TypeConverter
    fun toDate(dateLong: Long) = Date(dateLong)

    @TypeConverter
    fun fromDate(date: Date) = date.time

    @TypeConverter
    fun toEventType(value: String) = enumValueOf<EventType>(value)

    @TypeConverter
    fun fromEventType(value: EventType) = value.name

    @TypeConverter
    fun toNetworkState(value: String) = enumValueOf<NetworkState>(value)

    @TypeConverter
    fun fromNetworkState(value: NetworkState) = value.name
}
