package app.beachist.crew.database

import androidx.room.Entity

@Entity(tableName = "crew_info", primaryKeys = ["date"])
data class CrewInfo(
    val crew: String,
    val date: String,
) {
    // Crew names are personal data (GDPR); never expose them in logs/breadcrumbs.
    // Room and Gson use field access, so persistence and serialization are unaffected.
    override fun toString(): String = "CrewInfo(crew=REDACTED, date=$date)"
}
