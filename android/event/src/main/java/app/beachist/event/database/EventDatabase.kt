package app.beachist.event.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [(EventEntity::class)], version = 2)
@TypeConverters(EventConverters::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}
