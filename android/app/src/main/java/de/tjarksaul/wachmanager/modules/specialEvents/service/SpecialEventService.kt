package de.tjarksaul.wachmanager.modules.specialEvents.service

import de.tjarksaul.wachmanager.dtos.SpecialEvent
import de.tjarksaul.wachmanager.dtos.SpecialEventKind
import de.tjarksaul.wachmanager.modules.specialEvents.repository.SpecialEventRepository
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
