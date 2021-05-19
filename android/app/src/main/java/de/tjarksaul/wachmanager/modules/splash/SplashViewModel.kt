package de.tjarksaul.wachmanager.modules.splash

import android.annotation.SuppressLint
import de.tjarksaul.wachmanager.api.Async
import de.tjarksaul.wachmanager.dtos.Station
import de.tjarksaul.wachmanager.modules.base.BaseViewModel
import de.tjarksaul.wachmanager.modules.base.ViewModelAction
import de.tjarksaul.wachmanager.modules.base.ViewModelEffect
import de.tjarksaul.wachmanager.modules.base.ViewModelState
import de.tjarksaul.wachmanager.modules.station.GetStationsUseCase
import de.tjarksaul.wachmanager.repositories.StationRepository
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

internal class SplashViewModel(
    val getStationsUseCase: GetStationsUseCase,
    val stationRepository: StationRepository
) : BaseViewModel<SplashViewAction, SplashViewState, SplashViewEffect>(emptyState) {
    override fun handleActions() {
        disposables += actions.ofType<SplashViewAction.Refetch>()
            .switchMapWithRefetch()
            .subscribe { onRefetch() }

        disposables += actions.ofType<SplashViewAction.RefetchStoredStationId>()
            .subscribe { onRefetchStoredStationId() }

        disposables += actions.ofType<SplashViewAction.SelectStation>()
            .subscribe { onUpdateSelectedItem(it.index) }

        disposables += actions.ofType<SplashViewAction.UpdateCrewName>()
            .subscribe { onUpdateCrewName(it.name) }

        disposables += actions.ofType<SplashViewAction.Submit>()
            .subscribe { onSubmit() }
    }

    private fun <T> Observable<T>.switchMapWithRefetch() = this.switchMap {
        actions.ofType<SplashViewAction.Refetch>()
            .startWith(SplashViewAction.Refetch)
    }.debounce(300, TimeUnit.MILLISECONDS)


    private fun onRefetch() {
        onUpdateDate()

        disposables += stationRepository.getCachedStations()
            .subscribe { result ->
                if (result.count() > 0) {
                    state.set { copy(stations = result) }

                    actions.onNext(SplashViewAction.RefetchStoredStationId)
                }
            }

        disposables += getStationsUseCase()
            .subscribe { result ->
                when (result) {
                    is Async.Success -> {
                        state.set { copy(stations = result.data) }
                        disposables += stationRepository.cacheStations(result.data)
                            .subscribe { Timber.d("cached stations") }

                        actions.onNext(SplashViewAction.RefetchStoredStationId)
                    }
                }
            }
    }

    private fun onUpdateCrewName(name: String) {
        state.set { copy(crewName = name) }
    }

    private fun onUpdateSelectedItem(index: Int) {
        state.set { copy(selectedStation = index) }
    }

    @SuppressLint("SimpleDateFormat")
    private fun onUpdateDate() {
        val format = SimpleDateFormat("dd.MM.yyyy")

        state.set { copy(currentDate = format.format(Date())) }
    }

    private fun onRefetchStoredStationId() {
        val stationId = stationRepository.getStoredStationId()
        state.get { viewState ->
            val index = viewState.stations.indexOfFirst { it.id == stationId }

            if (index >= 0) {
                state.set { copy(selectedStation = index) }
            }
        }
    }

    private fun onSubmit() {
        state.get {
            val crewNames = it.crewName
            val stationIndex = it.selectedStation
            val stationId = it.stations[stationIndex].id

            stationRepository.saveStationId(stationId)
            stationRepository.saveCrew(crewNames)
            stationRepository.saveLastUpdateDate(Date().time)

            effects.onNext(SplashViewEffect.Dismiss)
        }
    }

    companion object {
        private val emptyState = SplashViewState()
    }

}

internal sealed class SplashViewAction : ViewModelAction {
    object Refetch : SplashViewAction()
    object RefetchStoredStationId : SplashViewAction()
    object Submit: SplashViewAction()

    data class SelectStation(val index: Int) : SplashViewAction()
    data class UpdateCrewName(val name: String) : SplashViewAction()
}

internal sealed class SplashViewEffect : ViewModelEffect {
    object Dismiss : SplashViewEffect()
}

internal data class SplashViewState(
    val crewName: String = "",
    val stations: List<Station> = listOf(),
    val selectedStation: Int = 0,
    val currentDate: String = ""
) : ViewModelState