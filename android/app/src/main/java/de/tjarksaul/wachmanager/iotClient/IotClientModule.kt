package de.tjarksaul.wachmanager.iotClient

import com.google.gson.Gson
import org.koin.dsl.module.module

val iotClientModule = module {
    single {
        IotClient(gson = Gson())
    }
}
