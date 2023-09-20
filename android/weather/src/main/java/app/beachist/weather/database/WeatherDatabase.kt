package app.beachist.weather.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import app.beachist.shared.date.DateFormatProvider
import java.util.*

@Database(
    entities = [DbAirInfo::class, DbWaterInfo::class, DbUvInfo::class],
    version = 1,
)
@TypeConverters(WeatherConverters::class)
internal abstract class WeatherDatabase: RoomDatabase() {
    abstract fun airInfoDao(): AirInfoDao
    abstract fun waterInfoDao(): WaterInfoDao
    abstract fun uvInfoDao(): UvInfoDao
}

class WeatherConverters {
    private val dateFormatProvider = DateFormatProvider()

    @TypeConverter
    fun toDate(dateString: String): Date? = dateFormatProvider.getIso8601DateTimeFormat().parse(dateString)

    @TypeConverter
    fun fromDate(date: Date): String = dateFormatProvider.getIso8601DateTimeFormat().format(date)
}
