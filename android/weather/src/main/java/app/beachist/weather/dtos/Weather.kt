package app.beachist.weather.dtos

import java.util.*

data class WaterInfo (
    val waterTemp: Double,
    val timestamp: Date,
)

data class UvInfo (
    val uv: Double,
    val maxUv: Double,
    val timestamp: Date,
)

data class AirInfo (
    val temperature: Double,
    val windBft: Int,
    val windDirection: String,
    val timestamp: Date,
)