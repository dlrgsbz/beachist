package app.beachist.special_event

import androidx.room.Room
import app.beachist.special_event.add.AddSpecialEventViewModel
import app.beachist.special_event.database.SpecialEventDatabase
import app.beachist.special_event.list.SpecialEventsViewModel
import app.beachist.special_event.repository.SpecialEventRepository
import app.beachist.special_event.repository.SpecialEventRepositoryImpl
import app.beachist.special_event.service.SpecialEventService
import app.beachist.special_event.sync.SyncSpecialEvents
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

@ExperimentalCoroutinesApi
val specialEventsModule = module {
    single(createdAtStart = true) {
        SyncSpecialEvents(get(), get(), get(), get(), get())
    }

    single {
        Room.databaseBuilder(get(), SpecialEventDatabase::class.java, "special_events")
            .build()
    }

    single {
        val db: SpecialEventDatabase = get()
        db.specialEventDao()
    }

    factory {
        SpecialEventRepositoryImpl(get(), get())
    } bind SpecialEventRepository::class

    viewModel { SpecialEventsViewModel(get()) }

    viewModel { AddSpecialEventViewModel(get()) }

    factory { SpecialEventService(get()) }
}
