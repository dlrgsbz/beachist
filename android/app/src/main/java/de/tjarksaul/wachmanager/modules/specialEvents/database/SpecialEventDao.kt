package de.tjarksaul.wachmanager.modules.specialEvents.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.beachist.shared.NetworkState
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SpecialEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(specialEvent: DbSpecialEvent): Long

    @Query("DELETE FROM special_event")
    suspend fun removeAll()

    @Query("SELECT * FROM special_event WHERE date LIKE :date")
    fun getSpecialEvents(date: String): Flow<List<DbSpecialEvent>>

    @Query("UPDATE special_event SET networkState = :networkState WHERE id = :id")
    suspend fun updateNetworkState(id: String, networkState: NetworkState): Int
}
