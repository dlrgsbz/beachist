package app.beachist.crew

import androidx.room.Room
import app.beachist.crew.api.CrewApiImpl
import app.beachist.crew.database.CrewDatabase
import app.beachist.crew.repository.CrewRepository
import app.beachist.crew.repository.CrewRepositoryImpl
import app.beachist.crew.ui.CrewNameViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

@ExperimentalCoroutinesApi()
val crewModule = module {
    single {
        CrewRepositoryImpl(get(), get())
    } bind CrewRepository::class

    single {
        Room.databaseBuilder(get(), CrewDatabase::class.java, "crew")
            .fallbackToDestructiveMigrationFrom(VERSION_1)
            .build()
    }

    single {
        val db: CrewDatabase = get()
        db.crewInfoDao()
    }

    single(createdAtStart = true) {
        CrewApiImpl(get(), get(), get(), get())
    }

    viewModel { CrewNameViewModel(get(), get(), get()) }
}

private const val VERSION_1 = 1
