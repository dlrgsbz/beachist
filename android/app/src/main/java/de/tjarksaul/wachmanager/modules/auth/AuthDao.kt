package de.tjarksaul.wachmanager.modules.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Observable

@Dao
internal abstract class AuthDao {
    @Query("SELECT * FROM certificate LIMIT 1")
    abstract fun queryOne(): Observable<Certificate>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(vararg certificate: Certificate): List<Long>

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(vararg certificate: Certificate): Int

    @Query("DELETE FROM certificate WHERE thingName != :thingName")
    abstract fun deleteAllExcept(thingName: String)

    @Query("DELETE FROM certificate")
    abstract fun deleteAll()

    @Transaction
    open fun upsert(vararg certificates: Certificate) {
        val insertIds = insert(*certificates)
        val updates = certificates.filterIndexed { i, _ -> insertIds[i] == -1L }
        update(*updates.toTypedArray())
    }
}
