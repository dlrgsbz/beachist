package de.tjarksaul.wachmanager

import org.koin.dsl.module.module

val globalKoinModule = module {
    single {
        GlobalStore(
            stationRepository = get(),
            createEvent = get(),
            eventRepository = get()
        )
    }

}