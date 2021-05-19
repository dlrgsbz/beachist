package de.tjarksaul.wachmanager.modules.station

import org.koin.dsl.module.module

val stationKoinModule = module {
    factory(override = true) {
        GetStationsUseCaseImpl(
            retrofit = get()
        ) as GetStationsUseCase
    }
}