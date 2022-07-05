package app.beachist.event

import androidx.room.Room
import app.beachist.event.database.EventDatabase
import app.beachist.event.repository.EventBackendRepository
import app.beachist.event.repository.EventRepository
import app.beachist.event.ui.EventViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val eventsModule = module {
    viewModel {
        EventViewModel(
            get(), get()
        )
    }

    factory {
        EventBackendRepository(get(), get(), get())
    }

    single {
        Room.databaseBuilder(get(), EventDatabase::class.java, "events")
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
