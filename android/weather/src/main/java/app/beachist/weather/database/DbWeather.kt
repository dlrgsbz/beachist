package app.beachist.weather.database

import androidx.room.Entity
import app.beachist.weather.dtos.AirInfo
import app.beachist.weather.dtos.UvInfo
import app.beachist.weather.dtos.WaterInfo
import java.util.Date

@Entity(tableName = "air", primaryKeys = ["date"])
internal data class DbAirInfo(
    val date: String,
    val temperature: Double,
    val windBft: Int,
    val windDirection: String,
    val timestamp: Date,
) {
    fun toAirInfo(): AirInfo {
        return AirInfo(
            temperature, windBft, windDirection, timestamp,
        )
    }
}

@Entity(tableName = "water", primaryKeys = ["date"])
internal data class DbWaterInfo(
    val date: String,
    val waterTemp: Double,
    val timestamp: Date,
) {
    fun toWaterInfo(): WaterInfo {
        return WaterInfo(
            waterTemp, timestamp,
        )
    }
}

@Entity(tableName = "uv", primaryKeys = ["date"])
internal data class DbUvInfo(
    val date: String,
    val uv: Double,
    val maxUv: Double,
    val timestamp: Date,
) {
    fun toUvInfo(): UvInfo {
        return UvInfo(
            uv, maxUv, timestamp,
        )
    }
}
