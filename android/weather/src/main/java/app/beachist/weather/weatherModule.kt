package app.beachist.weather

import androidx.room.Room
import app.beachist.weather.database.WeatherDatabase
import app.beachist.weather.repository.WeatherRepository
import app.beachist.weather.repository.WeatherRepositoryImpl
import app.beachist.weather.service.WeatherService
import app.beachist.weather.view.WeatherViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

@ExperimentalCoroutinesApi
val weatherModule = module {
    single {
        Room.databaseBuilder(get(), WeatherDatabase::class.java, "weather")
            .build()
    }

    single {
        val db: WeatherDatabase = get()
        db.airInfoDao()
    }

    single {
        val db: WeatherDatabase = get()
        db.waterInfoDao()
    }

    single {
        val db: WeatherDatabase = get()
        db.uvInfoDao()
    }

    factory {
        WeatherRepositoryImpl(get(), get(), get(), get())
    } bind WeatherRepository::class

    viewModel { WeatherViewModel(get()) }

    single(createdAtStart = true) { WeatherService(get(), get()) }
}
