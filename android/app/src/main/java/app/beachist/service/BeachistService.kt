package app.beachist.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import timber.log.Timber

class BeachistService: LifecycleService(), KoinComponent {
    private val serviceViewModel: ServiceViewModel by inject()

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service = this@BeachistService
    }

    init {
        Timber.tag("BeachistService").d("Init")
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Timber.tag("BeachistService").d("Bind")
        stopForeground(true)
        // The service is bound with BIND_AUTO_CREATE, so it stays alive while a client is bound.
        // Starting it here additionally keeps it alive after unbind, but on Android 12+ this throws
        // BackgroundServiceStartNotAllowedException when onBind is delivered while the app is in the
        // background. In that case we simply skip the start and rely on the binding to keep it alive.
        try {
            startService(intent)
        } catch (e: IllegalStateException) {
            Timber.tag("BeachistService").w(e, "Could not start service from background")
        }

        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onRebind(intent: Intent?) {
        stopForeground(true)

        super.onRebind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.tag("BeachistService").d("Create")

        serviceViewModel.updates.observe(this, {})
    }

    override fun onUnbind(intent: Intent): Boolean {
        return true
    }
}