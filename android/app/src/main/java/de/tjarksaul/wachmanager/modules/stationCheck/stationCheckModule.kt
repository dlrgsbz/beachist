package de.tjarksaul.wachmanager.modules.stationCheck

import com.google.gson.Gson
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val stationCheckModule = module {
    single { EntryService(get(), get(), Gson()) }
    single { FieldService(get(), get(), Gson()) }
    single { FieldLocalRepository(Gson(), get()) }
    viewModel { StationCheckViewModel(get(), get(), get(), get()) }
}
