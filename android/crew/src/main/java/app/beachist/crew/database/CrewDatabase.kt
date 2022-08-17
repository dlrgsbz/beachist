package app.beachist.crew.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CrewInfo::class],
    version = 2,
)
internal abstract class CrewDatabase : RoomDatabase() {
    abstract fun crewInfoDao(): CrewInfoDao
}
