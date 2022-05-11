package de.tjarksaul.wachmanager.modules.specialEvents

import de.tjarksaul.wachmanager.dtos.SpecialEventKind
import io.reactivex.Observable

class SpecialEventService {
    fun createSpecialEvent(title: String, note: String, notifier: String, kind: SpecialEventKind) {
        // create event
        // store in local rooms db
        // send message to mqtt broker
    }
}
