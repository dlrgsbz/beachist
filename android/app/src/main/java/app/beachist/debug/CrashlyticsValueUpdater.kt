package app.beachist.debug

import app.beachist.auth.repository.AuthRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class CrashlyticsValueUpdater(
    private val authRepository: AuthRepository,
    private val crashlytics: FirebaseCrashlytics,
    private val isDebug: Boolean,
) : CoroutineScope {
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + parentJob

    init {
        Timber.tag("CrashlyticsValueUpdater").i("launched")
        observeThingName()
        disableCrashlytics()
    }

    private fun observeThingName() {
        authRepository.getStateFlow()
            .onEach {
                val thingName = it.certificate?.thingName
                crashlytics.setCustomKey("station", thingName ?: "none")
            }
            .launchIn(this)
    }

    private fun disableCrashlytics() {
        if (isDebug) {
            Timber.tag("CrashlyticsValueUpdater").d("Disabling Crashlytics")
            crashlytics.setCrashlyticsCollectionEnabled(false)
        }
    }
}
