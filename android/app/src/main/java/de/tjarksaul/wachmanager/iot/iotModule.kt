package de.tjarksaul.wachmanager.iot

import com.google.gson.Gson
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val iotModule = module {
    single {
        IotRepositoryImpl(
            gson = Gson(),
            iotClient =  get(),
            versionRepo = get()
        )
    }  bind IotRepository::class
    viewModel { IotConnectionStateViewModel(get()) }
}
