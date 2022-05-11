package de.tjarksaul.wachmanager.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import de.tjarksaul.wachmanager.dtos.Station
import io.reactivex.Observable

abstract class StationRepository {
    abstract fun getStoredStationId(): String?
    abstract fun getStationId(): String
    abstract fun getCachedStations(): Observable<List<Station>>
    abstract fun hasCrew(): Boolean
    abstract fun getCrew(): String
    abstract fun getLastUpdateDate(): Long

    abstract fun cacheStations(stations: List<Station>): Observable<Unit>
    abstract fun saveStationId(stationId: String)
    abstract fun saveCrew(crew: String)
    abstract fun saveLastUpdateDate(date: Long)
}

class StationRepositoryImpl(
    val context: Context,
    val gson: Gson,
    val sharedPreferences: SharedPreferences
) : StationRepository() {
    companion object {
        private const val KeyStationId = "stationId"
        private const val KeyCachedStations = "cachedStations"
        private const val KeyCrew = "crewNames"
        private const val KeyLastStationSelectedDate = "lastStationSelectedDate"
    }

    override fun saveStationId(stationId: String) {
        sharedPreferences.edit {
            this.putString(KeyStationId, stationId)
        }
    }

    override fun getStoredStationId(): String? {
        return sharedPreferences.getString(KeyStationId, null)
    }

    override fun getStationId(): String {
        return getStoredStationId()
            ?: throw IllegalStateException("No stationId found.")
    }

    override fun getCachedStations(): Observable<List<Station>> {
        return Observable.create {
            val value = sharedPreferences.getString(KeyCachedStations, null)

            if (value == null) {
                it.onNext(listOf())
            } else {
                try {
                    val list = gson.fromJson(value, Array<Station>::class.java).toList()
                    it.onNext(list)
                } catch (ex: JsonSyntaxException) {
                    it.onNext(listOf())
                }
            }
        }

    }

    override fun cacheStations(stations: List<Station>): Observable<Unit> {
        return Observable.create {
            val stationString = gson.toJson(stations)

            sharedPreferences.edit {
                this.putString(KeyCachedStations, stationString)
            }
        }
    }

    override fun hasCrew(): Boolean {
        return getCrew().trim() !== ""
    }

    override fun getCrew(): String {
        return sharedPreferences.getString(KeyCrew, "") ?: ""
    }

    override fun getLastUpdateDate(): Long {
        return sharedPreferences.getLong(KeyLastStationSelectedDate, 0)
    }

    override fun saveCrew(crew: String) {
        sharedPreferences.edit {
            this.putString(KeyCrew, crew)
        }
    }

    override fun saveLastUpdateDate(date: Long) {
        sharedPreferences.edit {
            this.putLong(KeyLastStationSelectedDate, date)
        }
    }
}
