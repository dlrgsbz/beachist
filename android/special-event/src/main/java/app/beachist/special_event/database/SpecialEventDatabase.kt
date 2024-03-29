package app.beachist.special_event.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import app.beachist.shared.date.DateFormatProvider
import java.util.*

@Database(
    entities = [DbSpecialEvent::class],
    version = 2,
)
@TypeConverters(SpecialEventConverters::class)
internal abstract class SpecialEventDatabase: RoomDatabase() {
    abstract fun specialEventDao(): SpecialEventDao
}

class SpecialEventConverters {
    private val dateFormatProvider = DateFormatProvider()

    @TypeConverter
    fun toDate(dateString: String): Date? = dateFormatProvider.getIso8601DateTimeFormat().parse(dateString)

    @TypeConverter
    fun fromDate(date: Date): String = dateFormatProvider.getIso8601DateTimeFormat().format(date)
}
