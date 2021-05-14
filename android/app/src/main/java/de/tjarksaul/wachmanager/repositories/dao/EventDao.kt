package de.tjarksaul.wachmanager.repositories.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.tjarksaul.wachmanager.repositories.entities.EventEntity
import java.util.*

@Dao
interface EventDao {
    @Query("SELECT * FROM evententity WHERE date BETWEEN :startDate AND :endDate")
    fun getAllBetween(startDate: Date, endDate: Date): List<EventEntity>

    @Insert
    fun insert(event: EventEntity)

    @Update
    fun update(event: EventEntity)
}