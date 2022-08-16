package app.beachist

import app.beachist.debug.CrashInfoValueUpdater
import app.beachist.debug.CrashRecorder
import app.beachist.debug.FirebaseCrashRecorder
import app.beachist.service.ServiceViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    factory { ServiceViewModel(get(), get()) }

    single(createdAtStart = true) { CrashInfoValueUpdater(get(), get(), BuildConfig.DEBUG) }

    factory { FirebaseCrashRecorder(FirebaseCrashlytics.getInstance()) } bind CrashRecorder::class
}
