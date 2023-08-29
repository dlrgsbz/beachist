package app.beachist.weather.repository

import app.beachist.shared.date.DateFormatProvider
import app.beachist.weather.database.AirInfoDao
import app.beachist.weather.database.DbAirInfo
import app.beachist.weather.database.DbUvInfo
import app.beachist.weather.database.DbWaterInfo
import app.beachist.weather.database.UvInfoDao
import app.beachist.weather.database.WaterInfoDao
import app.beachist.weather.dtos.AirInfo
import app.beachist.weather.dtos.UvInfo
import app.beachist.weather.dtos.WaterInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.DateFormat
import java.time.LocalDate

interface WeatherRepository {
    fun getAirInfo(date: LocalDate): Flow<AirInfo?>
    fun getWaterInfo(date: LocalDate): Flow<WaterInfo?>
    fun getUvInfo(date: LocalDate): Flow<UvInfo?>

    suspend fun saveAirInfo(airInfo: AirInfo)
    suspend fun saveWaterInfo(waterInfo: WaterInfo)
    suspend fun saveUvInfo(uvInfo: UvInfo)
}

@Suppress("UNNECESSARY_SAFE_CALL")
internal class WeatherRepositoryImpl(
    private val airInfoDao: AirInfoDao,
    private val waterInfoDao: WaterInfoDao,
    private val uvInfoDao: UvInfoDao,
    private val dateFormatProvider: DateFormatProvider,
) : WeatherRepository {
    override fun getAirInfo(date: LocalDate): Flow<AirInfo?> =
        airInfoDao.getAirInfo(date.toString()).map { it?.toAirInfo() }

    override fun getWaterInfo(date: LocalDate): Flow<WaterInfo?> =
        waterInfoDao.getWaterInfo(date.toString()).map { it?.toWaterInfo() }

    override fun getUvInfo(date: LocalDate): Flow<UvInfo?> =
        uvInfoDao.getUvInfo(date.toString()).map { it?.toUvInfo() }

    override suspend fun saveAirInfo(airInfo: AirInfo) {
        airInfoDao.upsert(airInfo.toDbAirInfo(dateFormatProvider.getIso8601DateFormat()))
    }

    override suspend fun saveWaterInfo(waterInfo: WaterInfo) {
        waterInfoDao.upsert(waterInfo.toDbWaterInfo(dateFormatProvider.getIso8601DateFormat()))
    }

    override suspend fun saveUvInfo(uvInfo: UvInfo) {
        uvInfoDao.upsert(uvInfo.toDbUvInfo(dateFormatProvider.getIso8601DateFormat()))
    }
}

private fun AirInfo.toDbAirInfo(dateFormat: DateFormat): DbAirInfo =
    DbAirInfo(dateFormat.format(this.timestamp), this.temperature, this.windBft, this.windDirection, this.timestamp)

private fun WaterInfo.toDbWaterInfo(dateFormat: DateFormat): DbWaterInfo =
    DbWaterInfo(dateFormat.format(this.timestamp), this.waterTemp, this.timestamp)

private fun UvInfo.toDbUvInfo(dateFormat: DateFormat): DbUvInfo =
    DbUvInfo(dateFormat.format(this.timestamp), this.uv, this.maxUv, this.timestamp)