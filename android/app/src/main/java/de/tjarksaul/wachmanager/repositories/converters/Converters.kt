package de.tjarksaul.wachmanager.repositories.converters

import androidx.room.TypeConverter
import de.tjarksaul.wachmanager.dtos.EventType
import de.tjarksaul.wachmanager.dtos.NetworkState
import java.util.*

class Converters {
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
