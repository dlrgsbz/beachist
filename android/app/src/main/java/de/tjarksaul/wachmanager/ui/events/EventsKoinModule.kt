package de.tjarksaul.wachmanager.ui.events

import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val eventsKoinModule = module {
    viewModel(override = true) {
        EventViewModel(
            globalStore = get()
        )
    }

    factory(override = true) {
        CreateEventUsecaseImpl(
            retrofit = get()
        ) as CreateEventUseCase
    }
}
