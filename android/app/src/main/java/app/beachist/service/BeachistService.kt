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
        startService(intent)

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