package app.beachist.weather.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.beachist.weather.dtos.WaterInfo
import kotlinx.coroutines.flow.Flow

@Dao
internal interface WaterInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(waterInfo: DbWaterInfo): Long

    @Query("SELECT * FROM water WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    fun getWaterInfo(date: String): Flow<DbWaterInfo>
}
