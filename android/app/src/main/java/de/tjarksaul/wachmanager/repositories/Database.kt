package de.tjarksaul.wachmanager.repositories

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.tjarksaul.wachmanager.repositories.converters.Converters
import de.tjarksaul.wachmanager.repositories.dao.EventDao
import de.tjarksaul.wachmanager.repositories.entities.EventEntity

@Database(entities = [(EventEntity::class)], version = 1)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {
    abstract fun eventDao(): EventDao
}