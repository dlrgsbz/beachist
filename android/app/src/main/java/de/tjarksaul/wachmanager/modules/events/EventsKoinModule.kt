package de.tjarksaul.wachmanager.modules.events

import com.google.gson.Gson
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val eventsKoinModule = module {
    viewModel {
        EventViewModel(
            get(), get()
        )
    }

    factory {
        EventBackendRepository(get(), get(), Gson())
    }
}
