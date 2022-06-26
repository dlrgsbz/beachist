package de.tjarksaul.wachmanager.modules.specialEvents.database

import androidx.room.Entity
import de.tjarksaul.wachmanager.dtos.NetworkState
import de.tjarksaul.wachmanager.dtos.SpecialEvent
import de.tjarksaul.wachmanager.dtos.SpecialEventKind
import java.util.*

@Entity(tableName = "special_event", primaryKeys = ["id"])
internal data class DbSpecialEvent(
    val id: String,
    val title: String,
    val note: String,
    val notifier: String,
    val date: Date,
    val kind: SpecialEventKind,
    val networkState: NetworkState = NetworkState.pending,
) {
    fun toSpecialEvent(): SpecialEvent {
        return SpecialEvent(
            id, title, note, notifier, date, kind, networkState,
        )
    }
}
