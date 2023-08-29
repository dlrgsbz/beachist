package app.beachist.weather.dtos

import java.time.LocalDateTime

data class WeatherInfo(
    val airTemp: Int?,
    val windSpeed: Int?,
    val windDirection: String?,
    val waterTemp: Int?,
    val uvIndex: Int?,
    val maxUvIndex: Int?,
    val date: LocalDateTime?,
)