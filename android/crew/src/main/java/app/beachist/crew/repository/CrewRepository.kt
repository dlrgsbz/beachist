package app.beachist.crew.repository

import android.annotation.SuppressLint
import app.beachist.crew.database.CrewInfo
import app.beachist.crew.database.CrewInfoDao
import app.beachist.shared.base.tickerFlow
import app.beachist.shared.date.DateFormatProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.*
import kotlin.time.Duration.Companion.seconds

interface CrewRepository {
    fun hasCrew(): Flow<Boolean>
    fun getCrew(): Flow<String?>
    fun getCrewInfo(): Flow<CrewInfo?>

    suspend fun saveCrew(crew: String)
}

@ExperimentalCoroutinesApi
class CrewRepositoryImpl(
    private val crewInfoDao: CrewInfoDao,
    private val dateFormatProvider: DateFormatProvider,
) : CrewRepository {
    override fun hasCrew(): Flow<Boolean> {
        return getCrew().map { it !== null && it !== "" }
    }

    override fun getCrew(): Flow<String?> {
        return flowDate()
            .flatMapLatest {
                crewInfoDao.getCrew(it)
            }
            .map { it?.crew }
    }

    override fun getCrewInfo(): Flow<CrewInfo?> {
        return flowDate().flatMapLatest {
            crewInfoDao.getCrew(it)
        }
    }

    override suspend fun saveCrew(crew: String) {
        val crewInfo = CrewInfo(crew, getToday())

        crewInfoDao.upsert(crewInfo)
    }

    private fun flowDate(): Flow<String> {
        val format = dateFormatProvider.getIso8601DateFormat()
        return tickerFlow(30.seconds)
            .map { format.format(Date()) }
            .distinctUntilChanged()
    }

    @SuppressLint("SimpleDateFormat")
    private fun getToday(): String {
        val date = Calendar.getInstance().time
        return dateFormatProvider.getIso8601DateFormat().format(date)
    }
}
