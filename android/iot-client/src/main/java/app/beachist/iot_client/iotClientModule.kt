package app.beachist.iot_client

import app.beachist.iot_client.client.IotClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val iotClientModule = module {
    single {
        IotClient(gson = get(), tempDirectory = androidContext().cacheDir.absolutePath)
    }
    single { buildGson(get()) }
}
