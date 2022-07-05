package app.beachist.event.repository

import app.beachist.event.Event
import app.beachist.event.database.EventDatabase
import app.beachist.event.database.EventEntity
import io.reactivex.Observable
import java.util.*

class EventRepository(
    private val database: EventDatabase
) {
    private fun getEventsForToday(): List<EventEntity> {
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

        return database.eventDao().getAllBetween(start.time, end.time)
    }

    fun getEvents(): Observable<List<Event>> {
        return Observable.create { subscriber ->
            val dbEvents = getEventsForToday()
            val events = dbEvents.map {
                return@map Event(it.type, it.id, it.date, it.state)
            }
            subscriber.onNext(events)
            subscriber.onComplete()
        }
    }

    fun saveEvent(event: Event) {
        val dbEvent = EventEntity()
        dbEvent.id = event.id
        dbEvent.date = event.date
        dbEvent.type = event.type
        dbEvent.state = event.state
        database.eventDao().insert(dbEvent)
    }

    fun updateEvent(event: Event) {
        val dbEvents = getEventsForToday()
        val dbEvent = dbEvents.find { it.id == event.id }
        if (dbEvent == null) {
            saveEvent(event)
        } else {
            dbEvent.date = event.date
            dbEvent.type = event.type
            dbEvent.state = event.state

            database.eventDao().update(dbEvent)
        }
    }
}
