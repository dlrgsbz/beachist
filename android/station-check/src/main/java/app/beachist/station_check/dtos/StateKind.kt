package app.beachist.station_check.dtos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class StateKind : Parcelable {
    broken, tooLittle, other
}
