package app.beachist.debug

import timber.log.Timber

class TimberRemoteTree: Timber.DebugTree() {
    override fun log(priority: Int, t: Throwable?, message: String?, vararg args: Any?) {
        super.log(priority, t, message, *args)
    }
}