package de.tjarksaul.wachmanager.modules.auth

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [(Certificate::class)], version = 1)
internal abstract class AuthDatabase: RoomDatabase() {
    abstract fun authDao(): AuthDao
}
