package de.tjarksaul.wachmanager.modules.crew.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CrewInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(crewInfo: CrewInfo): Long

    @Query("DELETE FROM crew_info")
    suspend fun removeAll()

    @Query("SELECT * FROM crew_info WHERE date = :date")
    fun getCrew(date: String): Flow<CrewInfo?>
}
