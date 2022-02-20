package de.tjarksaul.wachmanager.service

import de.tjarksaul.wachmanager.BuildConfig

class StationNameProvider {
    fun currentStationName(): String? {
        return BuildConfig.AWS_IOT_CLIENT_ID
    }
}
