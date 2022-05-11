package de.tjarksaul.wachmanager.iotClient

import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val iotClientModule = module {
    single {
        IotClient(gson = Gson(), tempDirectory = androidContext().cacheDir.absolutePath)
    }
}
