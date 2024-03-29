package app.beachist.station_check.dtos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Entry(
    val id: String?,
    val field: String,
    val station: String,
    val state: Boolean?,
    val stateKind: StateKind?,
    val amount: Int?,
    val note: String?,
    val date: String // todo: date
) : Parcelable

