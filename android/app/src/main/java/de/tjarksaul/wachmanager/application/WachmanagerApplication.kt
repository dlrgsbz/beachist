package de.tjarksaul.wachmanager.application

import android.app.Application
import de.tjarksaul.wachmanager.api.apiKoinModule
import de.tjarksaul.wachmanager.globalKoinModule
import de.tjarksaul.wachmanager.modules.main.mainKoinModule
import de.tjarksaul.wachmanager.repositories.repositoryKoinModule
import de.tjarksaul.wachmanager.modules.events.eventsKoinModule
import org.koin.android.ext.android.startKoin

class WachmanagerApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin(
            androidContext = this,
            modules = listOf(
                repositoryKoinModule,
                apiKoinModule,
                eventsKoinModule,
                globalKoinModule,
                mainKoinModule
            )
        )
    }
}