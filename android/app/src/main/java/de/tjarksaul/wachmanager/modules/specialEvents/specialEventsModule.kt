package de.tjarksaul.wachmanager.modules.specialEvents

import androidx.room.Room
import com.google.gson.Gson
import de.tjarksaul.wachmanager.modules.specialEvents.add.AddSpecialEventViewModel
import de.tjarksaul.wachmanager.modules.specialEvents.database.SpecialEventDatabase
import de.tjarksaul.wachmanager.modules.specialEvents.list.SpecialEventsViewModel
import de.tjarksaul.wachmanager.modules.specialEvents.repository.SpecialEventRepository
import de.tjarksaul.wachmanager.modules.specialEvents.repository.SpecialEventRepositoryImpl
import de.tjarksaul.wachmanager.modules.specialEvents.service.SpecialEventService
import de.tjarksaul.wachmanager.modules.specialEvents.sync.SyncSpecialEvents
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
