package de.tjarksaul.wachmanager.service

import de.tjarksaul.wachmanager.modules.auth.AuthRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class StationNameProvider(
    private val authRepository: AuthRepository
) {
    private val disposable = CompositeDisposable()

    private var stationName: String? = null

    init {
        disposable += authRepository.getState()
            .subscribe { stationName = it?.certificate?.thingName }
    }

    fun currentStationName(): String? = stationName
}
