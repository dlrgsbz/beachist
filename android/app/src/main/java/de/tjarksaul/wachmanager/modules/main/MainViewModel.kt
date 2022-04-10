package de.tjarksaul.wachmanager.modules.main

import android.text.format.DateUtils
import de.tjarksaul.wachmanager.modules.auth.AuthRepository
import de.tjarksaul.wachmanager.modules.auth.State
import de.tjarksaul.wachmanager.modules.base.BaseViewModel
import de.tjarksaul.wachmanager.modules.base.ViewModelAction
import de.tjarksaul.wachmanager.modules.base.ViewModelEffect
import de.tjarksaul.wachmanager.modules.base.ViewModelState
import de.tjarksaul.wachmanager.repositories.StationRepository
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import java.util.concurrent.TimeUnit

internal class MainViewModel(
    private val stationRepository: StationRepository,
    private val authRepository: AuthRepository,
) : BaseViewModel<MainViewAction, MainViewState, MainViewEffect>(
    emptyState
) {
    override fun handleActions() {
        disposables += Observable.interval(1000L, TimeUnit.MILLISECONDS).timeInterval()
            .subscribe { actions.onNext(MainViewAction.CheckStationSelection) }

        disposables += actions.ofType<MainViewAction.CheckStationSelection>()
            .subscribe { onCheckStationSelection() }

        disposables += authRepository.getState()
            .subscribe { onAuthRepositoryState(it) }
    }

    private fun onAuthRepositoryState(authState: State?) {
        authState ?: return state.set {
            copy(currentView = MainViewCurrentView.Provision,
                shouldShowProvisioning = true)
        }

        authState.certificate ?: return state.set {
            copy(currentView = MainViewCurrentView.Provision,
                shouldShowProvisioning = true)
        }

        state.set { copy(shouldShowProvisioning = false) }

        actions.onNext(MainViewAction.CheckStationSelection)
    }

    private fun onCheckStationSelection() {
        val date = stationRepository.getLastUpdateDate()

        val shouldShowCrewInput = !DateUtils.isToday(date) || !stationRepository.hasCrew()

        state.get {
            val shouldShowProvisioning = it.shouldShowProvisioning

            val currentView = when {
                shouldShowProvisioning -> MainViewCurrentView.Provision
                shouldShowCrewInput -> MainViewCurrentView.CrewInput
                else -> MainViewCurrentView.TabbedView
            }

            state.set { copy(currentView = currentView) }
        }
    }

    companion object {
        private val emptyState = MainViewState()
    }
}

internal sealed class MainViewAction : ViewModelAction {
    object CheckStationSelection : MainViewAction()
}

internal sealed class MainViewEffect : ViewModelEffect

internal data class MainViewState(
    val currentView: MainViewCurrentView = MainViewCurrentView.TabbedView,
    val shouldShowProvisioning: Boolean = true,
) : ViewModelState

internal sealed class MainViewCurrentView {
    object TabbedView : MainViewCurrentView()
    object CrewInput : MainViewCurrentView()
    object Provision : MainViewCurrentView()
}
