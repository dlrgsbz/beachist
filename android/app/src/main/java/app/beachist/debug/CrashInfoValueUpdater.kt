package app.beachist.debug

import app.beachist.auth.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class CrashInfoValueUpdater(
    private val authRepository: AuthRepository,
    private val crashRecorder: CrashRecorder,
    private val isDebug: Boolean,
) : CoroutineScope {
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + parentJob

    init {
        Timber.tag("CrashInfoValueUpdater").i("launched")
        observeThingName()
        disableCrashlytics()
    }

    private fun observeThingName() {
        authRepository.getStateFlow()
            .onEach {
                val thingName = it.certificate?.thingName
                Timber.tag("CrashInfoValueUpdater").d("Updating thing name to $thingName")
                crashRecorder.setCustomKey("station", thingName ?: "none")
            }
            .launchIn(this)
    }

    private fun disableCrashlytics() {
        if (isDebug) {
            Timber.tag("CrashInfoValueUpdater").d("Disabling crash recording")
            crashRecorder.disable()
        }
    }
}
