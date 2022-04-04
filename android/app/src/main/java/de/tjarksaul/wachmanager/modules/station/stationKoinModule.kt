package de.tjarksaul.wachmanager.modules.station

import org.koin.dsl.module

val stationKoinModule = module {
    factory {
        GetStationsUseCaseImpl(
            retrofit = get()
        ) as GetStationsUseCase
    }
}