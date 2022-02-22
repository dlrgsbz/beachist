package de.tjarksaul.wachmanager.modules.events

import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

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
