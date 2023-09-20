package app.beachist.weather.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AirInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(airInfo: DbAirInfo): Long

    @Query("SELECT * FROM air WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    fun getAirInfo(date: String): Flow<DbAirInfo>
}
