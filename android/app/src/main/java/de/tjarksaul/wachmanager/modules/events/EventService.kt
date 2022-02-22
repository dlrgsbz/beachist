package de.tjarksaul.wachmanager.modules.events

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.koin.android.ext.android.inject
import org.koin.standalone.KoinComponent
import timber.log.Timber

class EventService: LifecycleService(), KoinComponent {
    protected val disposables = CompositeDisposable()

    private val eventViewModel: EventViewModel by inject()

    private val binder = LocalBinder()

    inner class LocalBinder: Binder() {
        val service = this@EventService
    }

    init {
        Timber.tag("EventService").d("Init")
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        Timber.tag("EventService").d("Bind")
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
        Timber.tag("EventService").d("Create")

        disposables += eventViewModel.states().subscribe {  }
    }

    override fun onUnbind(intent: Intent): Boolean {
        return true
    }
}