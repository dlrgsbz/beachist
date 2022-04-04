package de.tjarksaul.wachmanager.application

import android.app.Application
import de.tjarksaul.wachmanager.api.apiKoinModule
import de.tjarksaul.wachmanager.appModule
import de.tjarksaul.wachmanager.iot.iotModule
import de.tjarksaul.wachmanager.iotClient.iotClientModule
import de.tjarksaul.wachmanager.modules.events.eventsKoinModule
import de.tjarksaul.wachmanager.modules.main.mainKoinModule
import de.tjarksaul.wachmanager.modules.shared.sharedModule
import de.tjarksaul.wachmanager.modules.splash.splashKoinModule
import de.tjarksaul.wachmanager.modules.station.stationKoinModule
import de.tjarksaul.wachmanager.modules.stationCheck.stationCheckModule
import de.tjarksaul.wachmanager.repositories.repositoryKoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class WachmanagerApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@WachmanagerApplication.applicationContext)
            modules(
                repositoryKoinModule,
                apiKoinModule,
                eventsKoinModule,
                mainKoinModule,
                stationKoinModule,
                stationCheckModule,
                splashKoinModule,
                iotClientModule,
                iotModule,
                sharedModule,
                appModule
            )
        }

        Timber.plant(Timber.DebugTree())
    }
}
