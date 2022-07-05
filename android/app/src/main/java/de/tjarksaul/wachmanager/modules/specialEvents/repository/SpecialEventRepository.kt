package de.tjarksaul.wachmanager.modules.specialEvents.repository

import app.beachist.shared.NetworkState
import de.tjarksaul.wachmanager.dtos.SpecialEvent
import de.tjarksaul.wachmanager.modules.specialEvents.database.DbSpecialEvent
import de.tjarksaul.wachmanager.modules.specialEvents.database.SpecialEventDao
import app.beachist.shared.date.DateFormatProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

interface SpecialEventRepository {
    fun getSpecialEvents(date: Date): Flow<List<SpecialEvent>>

    fun saveSpecialEvent(specialEvent: SpecialEvent)
    suspend fun updateNetworkState(id: String, networkState: NetworkState)
}

internal class SpecialEventRepositoryImpl(
    private val dao: SpecialEventDao,
    private val dateFormatProvider: DateFormatProvider,
) : SpecialEventRepository {
    override fun getSpecialEvents(date: Date): Flow<List<SpecialEvent>> {
        val formattedDate = dateFormatProvider.getIso8601DateFormat().format(date)
        return dao.getSpecialEvents("$formattedDate%").map {
            it.map { event -> event.toSpecialEvent() }
        }
    }

    override fun saveSpecialEvent(specialEvent: SpecialEvent) {
        val event = specialEvent.toDbSpecialEvent()
        dao.upsert(event)
    }

    override suspend fun updateNetworkState(id: String, networkState: NetworkState) {
        dao.updateNetworkState(id, networkState)
    }
}

private fun SpecialEvent.toDbSpecialEvent(): DbSpecialEvent =
    DbSpecialEvent(id, title, note, notifier, date, kind, networkState)
