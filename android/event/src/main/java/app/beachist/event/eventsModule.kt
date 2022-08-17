package app.beachist.event

import androidx.room.Room
import app.beachist.event.database.EventDatabase
import app.beachist.event.repository.EventRepository
import app.beachist.event.sync.SyncEvents
import app.beachist.event.ui.EventViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@ExperimentalCoroutinesApi
val eventsModule = module {
    single(createdAtStart = true) {
        SyncEvents(get(), get(), get(), get(), get())
    }

    viewModel {
        EventViewModel(get())
    }

    single {
        Room.databaseBuilder(get(), EventDatabase::class.java, "events")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigrationFrom(VERSION_1)
            .build()
    }

    single {
        val db: EventDatabase = get()
        db.eventDao()
    }


    single {
        EventRepository(get())
    }
}

private const val VERSION_1 = 1
