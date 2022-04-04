package de.tjarksaul.wachmanager.modules.splash

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val splashKoinModule = module {
    viewModel {
        SplashViewModel(
            stationRepository = get(),
            getStationsUseCase = get()
        )
    }
}