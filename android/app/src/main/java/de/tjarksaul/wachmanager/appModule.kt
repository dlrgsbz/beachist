package de.tjarksaul.wachmanager

import de.tjarksaul.wachmanager.service.ServiceViewModel
import de.tjarksaul.wachmanager.service.StationNameProvider
import org.koin.dsl.module

val appModule = module {
    factory { ServiceViewModel(get(), get()) }
    factory { StationNameProvider(get()) }
}
