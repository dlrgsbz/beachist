package app.beachist

import android.app.Application
import app.beachist.shared.sharedModule
import de.tjarksaul.wachmanager.appModule
import app.beachist.iot.iotModule
import app.beachist.iot_client.iotClientModule
import app.beachist.auth.authModule
import app.beachist.crew.crewModule
import app.beachist.event.eventsModule
import de.tjarksaul.wachmanager.modules.main.mainKoinModule
import app.beachist.provision.provisionModule
import de.tjarksaul.wachmanager.modules.specialEvents.specialEventsModule
import de.tjarksaul.wachmanager.modules.stationCheck.stationCheckModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

@ExperimentalCoroutinesApi
class BeachistApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BeachistApp.applicationContext)
            modules(
                eventsModule,
                mainKoinModule,
                stationCheckModule,
                iotClientModule,
                iotModule,
                sharedModule,
                provisionModule,
                authModule,
                crewModule,
                specialEventsModule,
                appModule,
            )
        }

        Timber.plant(Timber.DebugTree())
    }
}
