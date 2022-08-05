package app.beachist

import app.beachist.service.ServiceViewModel
import org.koin.dsl.module

val appModule = module {
    factory { ServiceViewModel(get(), get()) }
}
