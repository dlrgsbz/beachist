package de.tjarksaul.wachmanager.modules.auth

import androidx.room.Room
import org.koin.dsl.module

val authModule = module {
    single {
        Room.databaseBuilder(get(), AuthDatabase::class.java, "auth")
            .fallbackToDestructiveMigration()
            .build()
    }
    single {
        val db: AuthDatabase = get()
        db.authDao()
    }
    single { AuthRepository(get()) }
}
