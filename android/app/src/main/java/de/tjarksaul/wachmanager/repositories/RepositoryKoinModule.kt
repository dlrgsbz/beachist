package de.tjarksaul.wachmanager.repositories

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import de.tjarksaul.wachmanager.BuildConfig
import org.koin.android.ext.koin.androidContext
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
            context = get(),
            gson = Gson(),
            sharedPreferences = androidContext().getSharedPreferences(
                BuildConfig.SHARED_PREFS_NAME,
                Context.MODE_PRIVATE
            )
        ) as StationRepository
    }

    single {
        EventRepository(
            database = get()
        )
    }
}
