package app.beachist.event.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import app.beachist.shared.NetworkState
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface EventDao {
    @Query("SELECT * FROM evententity WHERE date BETWEEN :startDate AND :endDate")
    fun getAllBetween(startDate: Date, endDate: Date): List<EventEntity>

    @Query("SELECT * FROM evententity WHERE date BETWEEN :startDate AND :endDate")
    fun observeAllBetween(startDate: Date, endDate: Date): Flow<List<EventEntity>>

    @Insert
    suspend fun insert(event: EventEntity)

    @Update
    fun update(event: EventEntity)

    @Query("UPDATE evententity SET state = :networkState WHERE id = :id")
    suspend fun updateNetworkState(id: String, networkState: NetworkState): Int
}
