package app.beachist.special_event.service

import app.beachist.special_event.dtos.SpecialEvent
import app.beachist.special_event.dtos.SpecialEventKind
import app.beachist.special_event.repository.SpecialEventRepository
import java.util.*

class SpecialEventService(
    private val repository: SpecialEventRepository,
    ) {
    fun createSpecialEvent(title: String, note: String, notifier: String, kind: SpecialEventKind) {
        val id = UUID.randomUUID().toString()

        val event = SpecialEvent(id, title, note, notifier, Date(), kind)

        repository.saveSpecialEvent(event)
    }
}
