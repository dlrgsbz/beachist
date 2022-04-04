package de.tjarksaul.wachmanager.modules.main

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainKoinModule = module {
    viewModel {
        MainViewModel(
            stationRepository = get()
        )
    }
}
