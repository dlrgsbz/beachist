package app.beachist.debug

import android.util.Log
import timber.log.Timber

/**
 * Forwards Timber logs to Crashlytics so that the most recent log messages are
 * attached to crash reports as breadcrumbs. Since every ViewModel action is
 * logged via Timber, this captures what the user was doing right before a crash
 * (e.g. entering checklist items). Errors carrying a throwable are additionally
 * recorded as non-fatal exceptions.
 */
class CrashReportingTree(private val crashRecorder: CrashRecorder) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE) return

        val entry = if (tag != null) "$tag: $message" else message
        crashRecorder.log(entry)

        if (t != null && priority >= Log.ERROR) {
            crashRecorder.recordException(t)
        }
    }
}
