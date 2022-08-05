package app.beachist.crew.repository

import android.annotation.SuppressLint
import app.beachist.crew.database.CrewInfo
import app.beachist.crew.database.CrewInfoDao
import app.beachist.shared.date.DateFormatProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

interface CrewRepository {
    fun hasCrew(): Flow<Boolean>
    fun getCrew(): Flow<String?>
    fun getCrewInfo(): Flow<CrewInfo?>

    fun saveCrew(crew: String)
}

class CrewRepositoryImpl(
    private val crewInfoDao: CrewInfoDao,
    private val dateFormatProvider: DateFormatProvider,
) : CrewRepository {
    override fun hasCrew(): Flow<Boolean> {
        return getCrew().map { it !== null && it !== "" }
    }

    override fun getCrew(): Flow<String?> {
        return crewInfoDao.getCrew(getToday()).map { it?.crew }
    }

    override fun getCrewInfo(): Flow<CrewInfo?> {
        return crewInfoDao.getCrew(getToday())
    }

    override fun saveCrew(crew: String) {
        val crewInfo = CrewInfo(crew, getToday())

        crewInfoDao.upsert(crewInfo)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getToday(): String {
        val date = Calendar.getInstance().time
        return dateFormatProvider.getIso8601DateFormat().format(date)
    }
}
