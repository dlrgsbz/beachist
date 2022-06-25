package de.tjarksaul.wachmanager.modules.crew.repository

import android.annotation.SuppressLint
import de.tjarksaul.wachmanager.modules.crew.database.CrewInfo
import de.tjarksaul.wachmanager.modules.crew.database.CrewInfoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

interface CrewRepository {
    fun hasCrew(): Flow<Boolean>
    fun getCrew(): Flow<String?>
    fun getCrewInfo(): Flow<CrewInfo?>

    fun saveCrew(crew: String)
}

class CrewRepositoryImpl(
    private val crewInfoDao: CrewInfoDao,
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
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(date)
    }
}
