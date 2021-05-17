package de.tjarksaul.wachmanager.modules.splash

import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val splashKoinModule = module {
    viewModel(override = true) {
        SplashViewModel(
            stationRepository = get(),
            getStationsUseCase = get()
        )
    }
}