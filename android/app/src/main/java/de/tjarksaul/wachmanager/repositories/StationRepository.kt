package de.tjarksaul.wachmanager.repositories

import android.content.Context

abstract class StationRepository {
    abstract fun getStoredStationId(): String?
    abstract fun getStationId(): String
    abstract fun getStationName(): String?
}

class StationRepositoryImpl(val context: Context): StationRepository() {
    override fun getStoredStationId(): String? {
        return context.getSharedPreferences("WachmanagerSettings", Context.MODE_PRIVATE)
            .getString("stationId", null)
    }

    override fun getStationId(): String {
        return getStoredStationId()
            ?: throw IllegalStateException("No stationId found.")
    }

    override fun getStationName(): String? {
        return context.getSharedPreferences("WachmanagerSettings", Context.MODE_PRIVATE)
            .getString("stationName", null) ?: throw IllegalStateException("No stationName found.")
    }
}