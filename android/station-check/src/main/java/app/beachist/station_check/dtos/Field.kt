package app.beachist.station_check.dtos

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Field(
    val id: String, // todo: id
    val name: String,
    val parent: String? = null,
    val required: Int? = null,
    val note: String? = null,
    var entry: Entry? = null
) : Parcelable
