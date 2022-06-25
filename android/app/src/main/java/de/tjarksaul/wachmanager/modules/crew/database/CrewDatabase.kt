package de.tjarksaul.wachmanager.modules.crew.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CrewInfo::class],
    version = 1,
)
internal abstract class CrewDatabase : RoomDatabase() {
    abstract fun crewInfoDao(): CrewInfoDao
}
