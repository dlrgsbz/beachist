package app.beachist.event.repository

import app.beachist.event.database.EventDao
import app.beachist.event.database.EventEntity
import app.beachist.event.dto.Event
import app.beachist.shared.NetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class EventRepository(
    private val dao: EventDao,
) {
    fun observeEvents(): Flow<List<Event>> {
        val (start, end) = getTimes()

        return dao.observeAllBetween(start, end).map { list ->
            list.map { Event(it.type, it.id, it.date, it.state) }
        }
    }

    suspend fun saveEvent(event: Event) {
        val dbEvent = event.toDbEvent()
        dao.insert(dbEvent)
    }

    suspend fun updateNetworkState(id: String, networkState: NetworkState) {
        dao.updateNetworkState(id, networkState)
    }
}

private fun getTimes(): Pair<Date, Date> {
    val start: Calendar = GregorianCalendar()
    start.set(Calendar.HOUR_OF_DAY, 0)
    start.set(Calendar.MINUTE, 0)
    start.set(Calendar.SECOND, 0)
    start.set(Calendar.MILLISECOND, 0)
    val end: Calendar = GregorianCalendar()
    end.set(Calendar.HOUR_OF_DAY, 23)
    end.set(Calendar.MINUTE, 59)
    end.set(Calendar.SECOND, 59)
    end.set(Calendar.MILLISECOND, 99)

    return Pair(start.time, end.time)
}

private fun Event.toDbEvent(): EventEntity {
    val dbEvent = EventEntity()
    dbEvent.id = this.id
    dbEvent.date = this.date
    dbEvent.type = this.type
    dbEvent.state = this.state
    return dbEvent
}
