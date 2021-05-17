package de.tjarksaul.wachmanager.modules.main

import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val mainKoinModule = module {
    viewModel(override = true) {
        MainViewModel(
            stationRepository = get()
        )
    }
}
