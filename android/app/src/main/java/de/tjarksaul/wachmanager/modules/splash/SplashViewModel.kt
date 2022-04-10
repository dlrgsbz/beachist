package de.tjarksaul.wachmanager.modules.splash

import android.annotation.SuppressLint
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.api.Async
import de.tjarksaul.wachmanager.dtos.Station
import de.tjarksaul.wachmanager.modules.auth.AuthRepository
import de.tjarksaul.wachmanager.modules.auth.State
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
    private val stationRepository: StationRepository,
    private val authRepository: AuthRepository,
) : BaseViewModel<SplashViewAction, SplashViewState, SplashViewEffect>(emptyState) {
    override fun handleActions() {
        disposables += actions.ofType<SplashViewAction.Refetch>()
            .switchMapWithRefetch()
            .subscribe { onRefetch() }

        disposables += actions.ofType<SplashViewAction.UpdateCrewName>()
            .subscribe { onUpdateCrewName(it.name) }

        disposables += actions.ofType<SplashViewAction.Submit>()
            .subscribe { onSubmit() }

        disposables += authRepository.getState()
            .subscribe { onAuthRepositoryState(it) }
    }

    private fun <T> Observable<T>.switchMapWithRefetch() = this.switchMap {
        actions.ofType<SplashViewAction.Refetch>()
            .startWith(SplashViewAction.Refetch)
    }.debounce(300, TimeUnit.MILLISECONDS)

    private fun onRefetch() {
        onUpdateDate()
    }

    private fun onAuthRepositoryState(authState: State?) {
        authState?.certificate ?: return

        val stationId = authState.certificate.thingName.split("-").last()

        state.set { copy(stationId = stationId) }
    }

    private fun onUpdateCrewName(name: String) {
        state.set { copy(crewName = name) }
    }

    @SuppressLint("SimpleDateFormat")
    private fun onUpdateDate() {
        val format = SimpleDateFormat("dd.MM.yyyy")

        state.set { copy(currentDate = format.format(Date())) }
    }

    private fun onSubmit() {
        state.get {
            val crewNames = it.crewName

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
    val stationId: String = "",
    val currentDate: String = "",
) : ViewModelState