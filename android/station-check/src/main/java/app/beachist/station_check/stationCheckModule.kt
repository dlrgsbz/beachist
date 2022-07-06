package app.beachist.station_check

import app.beachist.station_check.repository.FieldLocalRepository
import app.beachist.station_check.service.EntryService
import app.beachist.station_check.service.FieldService
import app.beachist.station_check.ui.StationCheckViewModel
import com.google.gson.Gson
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val stationCheckModule = module {
    single { EntryService(get(), get(), get()) }
    single { FieldService(get(), get(), get()) }
    single { FieldLocalRepository(Gson(), get()) }
    viewModel { StationCheckViewModel(get(), get(), get(), get()) }
}
