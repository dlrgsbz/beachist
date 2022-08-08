package app.beachist.main

import app.beachist.auth.repository.AuthRepository
import app.beachist.auth.repository.State
import app.beachist.shared.base.BaseViewModel
import app.beachist.shared.base.ViewModelAction
import app.beachist.shared.base.ViewModelEffect
import app.beachist.shared.base.ViewModelState
import app.beachist.crew.repository.CrewRepository
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi()
internal class MainViewModel(
    private val crewRepository: CrewRepository,
    private val authRepository: AuthRepository,
) : BaseViewModel<MainViewAction, MainViewState, MainViewEffect>(
    emptyState
), CoroutineScope {
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.IO + parentJob

    override fun handleActions() {
        disposables += actions.ofType<MainViewAction.CheckStationSelection>()
            .subscribe { onCheckStationSelection() }

        disposables += authRepository.getState()
            .subscribe { onAuthRepositoryState(it) }

        actions.onNext(MainViewAction.CheckStationSelection)
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
        crewRepository.hasCrew().mapLatest { hasCrew ->
            Timber.d("hasCrew: $hasCrew")
            val shouldShowCrewInput = !hasCrew
            state.get { currentState ->
                val shouldShowProvisioning = currentState.shouldShowProvisioning

                val currentView = when {
                    shouldShowProvisioning -> MainViewCurrentView.Provision
                    shouldShowCrewInput -> MainViewCurrentView.CrewInput
                    else -> MainViewCurrentView.TabbedView
                }

                launch(Dispatchers.Main) {
                    state.set { copy(currentView = currentView) }
                }
            }
        }.launchIn(this)
    }

    companion object {
        private val emptyState = MainViewState()
    }
}

internal sealed class MainViewAction : ViewModelAction() {
    object CheckStationSelection : MainViewAction()
}

internal sealed class MainViewEffect : ViewModelEffect()

internal data class MainViewState(
    val currentView: MainViewCurrentView = MainViewCurrentView.TabbedView,
    val shouldShowProvisioning: Boolean = true,
) : ViewModelState

internal sealed class MainViewCurrentView {
    object TabbedView : MainViewCurrentView()
    object CrewInput : MainViewCurrentView()
    object Provision : MainViewCurrentView()
}
