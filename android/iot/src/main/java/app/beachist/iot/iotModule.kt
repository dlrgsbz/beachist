package app.beachist.iot

import app.beachist.iot.repository.IotRepository
import app.beachist.iot.repository.IotRepositoryImpl
import app.beachist.iot.ui.IotConnectionStateViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val iotModule = module {
    single {
        IotRepositoryImpl(
            gson = get(),
            iotClient =  get(),
            versionRepo = get()
        )
    }  bind IotRepository::class
    viewModel { IotConnectionStateViewModel(get()) }
}
