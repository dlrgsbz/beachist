package de.tjarksaul.wachmanager.iot

import com.google.gson.Gson
import org.koin.dsl.module.module

val iotModule = module {
    single {
        IotRepositoryImpl(
            gson = Gson(),
            iotClient =  get(),
            versionRepo = get()
        )
    }  bind IotRepository::class
}