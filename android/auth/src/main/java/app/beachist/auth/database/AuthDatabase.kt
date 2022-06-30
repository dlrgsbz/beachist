package app.beachist.auth.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [(DbCertificate::class)], version = 2)
internal abstract class AuthDatabase: RoomDatabase() {
    abstract fun authDao(): AuthDao
}
