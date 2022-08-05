package app.beachist.auth

import androidx.room.Room
import app.beachist.auth.database.AuthDatabase
import app.beachist.auth.database.MIGRATION_1_TO_2
import app.beachist.auth.repository.AuthRepository
import app.beachist.auth.station.StationNameProvider
import org.koin.dsl.module

val authModule = module {
    single {
        Room.databaseBuilder(get(), AuthDatabase::class.java, "auth")
            .addMigrations(MIGRATION_1_TO_2)
            .fallbackToDestructiveMigration()
            .build()
    }
    single {
        val db: AuthDatabase = get()
        db.authDao()
    }
    single { AuthRepository(get()) }
    factory { StationNameProvider(get()) }
}
