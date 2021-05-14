package de.tjarksaul.wachmanager.repositories

import androidx.room.Room
import de.tjarksaul.wachmanager.BuildConfig
import org.koin.dsl.module.module

val repositoryKoinModule = module {
    single {
        Room.databaseBuilder(get(), Database::class.java, BuildConfig.DATABASE_NAME)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        StationRepositoryImpl(
            context = get()
        ) as StationRepository
    }

    single {
        EventRepository(
            database = get()
        )
    }
}
