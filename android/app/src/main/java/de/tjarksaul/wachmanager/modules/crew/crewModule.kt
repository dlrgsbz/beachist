package de.tjarksaul.wachmanager.modules.crew

import androidx.room.Room
import com.google.gson.Gson
import de.tjarksaul.wachmanager.modules.crew.api.CrewApiImpl
import de.tjarksaul.wachmanager.modules.crew.database.CrewDatabase
import de.tjarksaul.wachmanager.modules.crew.repository.CrewRepository
import de.tjarksaul.wachmanager.modules.crew.repository.CrewRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.dsl.module

@ExperimentalCoroutinesApi()
val crewModule = module {
    single {
        CrewRepositoryImpl(get()) as CrewRepository
    }

    single {
        Room.databaseBuilder(get(), CrewDatabase::class.java, "crew")
            .build()
    }

    single {
        val db: CrewDatabase = get()
        db.crewInfoDao()
    }

    single(createdAtStart = true) {
        CrewApiImpl(get(), get(), get(), Gson())
    }
}
