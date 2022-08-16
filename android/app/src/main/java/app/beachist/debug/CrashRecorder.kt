package app.beachist.debug

import com.google.firebase.crashlytics.FirebaseCrashlytics

interface CrashRecorder {
    fun recordException(throwable: Throwable)
    fun log(message: String)
    fun disable()
    fun setCustomKey(key: String, value: String)
}

class FirebaseCrashRecorder(private val crashlytics: FirebaseCrashlytics): CrashRecorder {
    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun disable() {
        crashlytics.setCrashlyticsCollectionEnabled(false)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
}
