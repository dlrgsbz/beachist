package de.tjarksaul.wachmanager.repositories

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import de.tjarksaul.wachmanager.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryKoinModule = module {
    single {
        Room.databaseBuilder(get(), Database::class.java, BuildConfig.DATABASE_NAME)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        EventRepository(
            database = get()
        )
    }
}
