package app.beachist.weather.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface UvInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(uvInfo: DbUvInfo): Long

    @Query("SELECT * FROM uv WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    fun getUvInfo(date: String): Flow<DbUvInfo>
}
