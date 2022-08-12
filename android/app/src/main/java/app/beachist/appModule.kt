package app.beachist

import app.beachist.debug.CrashlyticsValueUpdater
import app.beachist.service.ServiceViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.dsl.module

val appModule = module {
    factory { ServiceViewModel(get(), get()) }

    single(createdAtStart = true) { CrashlyticsValueUpdater(get(), FirebaseCrashlytics.getInstance(), BuildConfig.DEBUG) }
}
