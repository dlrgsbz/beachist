package app.beachist.weather.service

import app.beachist.iot_client.client.IotClient
import app.beachist.iot_client.client.IotConnectionState
import app.beachist.shared.base.tickerFlow
import app.beachist.weather.dtos.AirInfo
import app.beachist.weather.dtos.UvInfo
import app.beachist.weather.dtos.WaterInfo
import app.beachist.weather.dtos.WeatherInfo
import app.beachist.weather.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds


@ExperimentalCoroutinesApi
class WeatherService(
    private val iotClient: IotClient,
    private val weatherRepository: WeatherRepository,
) : CoroutineScope {
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + parentJob

    private val _weatherInfo: MutableStateFlow<WeatherInfo?> = MutableStateFlow(null)
    val weatherInfo: StateFlow<WeatherInfo?> = _weatherInfo

    init {
        subscribe()
    }

    private fun subscribe() {
        iotClient.observeConnectionState()
            .mapLatest {
                iotClient.unsubscribe("shared/weather/air") { status ->
                    Timber.tag("WeatherService").d("Unsubscribed from shared/weather/air with $status")
                }
                iotClient.unsubscribe("shared/weather/water") { status ->
                    Timber.tag("WeatherService").d("Unsubscribed from shared/weather/water with $status")
                }
                iotClient.unsubscribe("shared/weather/uvi") { status ->
                    Timber.tag("WeatherService").d("Unsubscribed from shared/weather/uvi with $status")
                }
                return@mapLatest it
            }
            .filter {
                it is IotConnectionState.Connected
            }
            .mapLatest {
                val topic = "shared/weather/air"
                iotClient.subscribeDecoding(topic, AirInfo::class.java, { status ->
                    Timber.tag("WeatherService").d("Subscribed to $topic with $status")
                }) { airInfo ->
                    Timber.tag("WeatherService").d("Got air info: $airInfo")
                    try {
                        onAirInfo(airInfo)
                    } catch (e: Exception) {
                        Timber.tag("WeatherService").e(e, "Error in onAirInfo")
                    }
                }
            }
            .mapLatest {
                val topic = "shared/weather/water"
                iotClient.subscribeDecoding(topic, WaterInfo::class.java, { status ->
                    Timber.tag("WeatherService").d("Subscribed to $topic with $status")
                }) { waterInfo ->
                    Timber.tag("WeatherService").d("Got water info: $waterInfo")
                    try {
                        onWaterInfo(waterInfo)
                    } catch (e: Exception) {
                        Timber.tag("WeatherService").e(e, "Error in onWaterInfo")
                    }
                }
            }
            .mapLatest {
                val topic = "shared/weather/uvi"
                iotClient.subscribeDecoding(topic, UvInfo::class.java, { status ->
                    Timber.tag("WeatherService").d("Subscribed to $topic with $status")
                }) { uvInfo ->
                    Timber.tag("WeatherService").d("Got UV info: $uvInfo")
                    try {
                        onUvInfo(uvInfo)
                    } catch (e: Exception) {
                        Timber.tag("WeatherService").e(e, "Error in onUvInfo")
                    }
                }
            }
            .catch {
                Timber.tag("WeatherService").e(it, "Error in subscribe")
            }
            .launchIn(this)

        flowDate()
            .flatMapLatest { date ->
                combine(
                    weatherRepository.getAirInfo(date),
                    weatherRepository.getWaterInfo(date),
                    weatherRepository.getUvInfo(date)
                ) { airInfo, waterInfo, uvInfo ->
                    WeatherInfo(
                        airTemp = airInfo?.temperature?.toInt(),
                        windSpeed = airInfo?.windBft,
                        windDirection = airInfo?.windDirection,
                        waterTemp = waterInfo?.waterTemp?.toInt(),
                        uvIndex = uvInfo?.uv?.roundToInt(),
                        maxUvIndex = uvInfo?.maxUv?.roundToInt(),
                        date = airInfo?.timestamp?.toLocalDateTime()
                            ?: waterInfo?.timestamp?.toLocalDateTime()
                            ?: uvInfo?.timestamp?.toLocalDateTime(),
                    )
                }
            }
            .mapLatest {
                _weatherInfo.value = it
            }
            .launchIn(this)
    }

    private fun flowDate(): Flow<LocalDate> {
        return tickerFlow(30.seconds)
            .map { LocalDate.now(ZoneId.systemDefault()) }
            .distinctUntilChanged()
    }

    private fun onAirInfo(airInfo: AirInfo) = launch {
        weatherRepository.saveAirInfo(airInfo)
    }


    private fun onWaterInfo(waterInfo: WaterInfo) = launch {
        weatherRepository.saveWaterInfo(waterInfo)
    }

    private fun onUvInfo(uvInfo: UvInfo) = launch {
        weatherRepository.saveUvInfo(uvInfo)
    }
}

private fun Date.toLocalDateTime(): LocalDateTime? {
    return this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}
