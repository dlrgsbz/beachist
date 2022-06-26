package de.tjarksaul.wachmanager.modules.specialEvents.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import de.tjarksaul.wachmanager.repositories.converters.Converters
import de.tjarksaul.wachmanager.util.DateFormatProvider
import java.util.*

@Database(
    entities = [DbSpecialEvent::class],
    version = 1,
)
@TypeConverters(SpecialEventConverters::class)
internal abstract class SpecialEventDatabase: RoomDatabase() {
    abstract fun specialEventDao(): SpecialEventDao
}

class SpecialEventConverters {
    private val dateFormatProvider = DateFormatProvider()

    @TypeConverter
    fun toDate(dateString: String) = dateFormatProvider.getIso8601DateTimeFormat().parse(dateString)

    @TypeConverter
    fun fromDate(date: Date) = dateFormatProvider.getIso8601DateTimeFormat().format(date)
}
