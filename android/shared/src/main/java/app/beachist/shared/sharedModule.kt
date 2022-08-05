package app.beachist.shared

import app.beachist.shared.date.DateFormatProvider
import org.koin.dsl.module

val sharedModule = module {
    factory { AppVersionRepository(get()) }
    factory { DateFormatProvider() }
}
