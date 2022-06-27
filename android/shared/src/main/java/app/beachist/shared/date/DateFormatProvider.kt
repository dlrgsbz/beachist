package app.beachist.shared.date

import android.annotation.SuppressLint
import android.text.format.DateUtils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class DateFormatProvider {
    fun getIso8601DateFormat(): DateFormat {
        return SimpleDateFormat("yyyy-MM-dd")
    }

    fun getIso8601DateTimeFormat(): DateFormat {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    }

    fun getTimeFormatForDate(date: Date): DateFormat {
        val format = if (DateUtils.isToday(date.time)) "HH:mm" else "dd.mm.YYYY HH:mm"
        return SimpleDateFormat(format)
    }

    fun getShortDateTimeFormat(): DateFormat {
        return SimpleDateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT
        )
    }
}
