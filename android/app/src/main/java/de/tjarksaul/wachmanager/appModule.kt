package de.tjarksaul.wachmanager

import de.tjarksaul.wachmanager.service.ServiceViewModel
import org.koin.dsl.module

val appModule = module {
    factory { ServiceViewModel(get(), get()) }
}
