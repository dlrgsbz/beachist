package app.beachist.special_event.database

import androidx.room.Entity
import app.beachist.shared.NetworkState
import app.beachist.special_event.dtos.SpecialEvent
import app.beachist.special_event.dtos.SpecialEventKind
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
