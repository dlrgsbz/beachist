package app.beachist.crew.ui

import androidx.lifecycle.viewModelScope
import app.beachist.auth.repository.AuthRepository
import app.beachist.auth.repository.State
import app.beachist.crew.repository.CrewRepository
import app.beachist.shared.base.FlowBaseViewModel
import app.beachist.shared.base.ViewModelAction
import app.beachist.shared.base.ViewModelEffect
import app.beachist.shared.base.ViewModelState
import app.beachist.shared.base.set
import app.beachist.shared.base.tickerFlow
import app.beachist.shared.date.DateFormatProvider
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.*
import kotlin.time.Duration.Companion.seconds

internal class CrewNameViewModel(
    private val crewRepository: CrewRepository,
    private val authRepository: AuthRepository,
    private val dateFormatProvider: DateFormatProvider,
) : FlowBaseViewModel<CrewNameViewAction, CrewNameViewState, CrewNameViewEffect>(emptyState) {
    override fun handleActions() {
        val format = dateFormatProvider.getIso8601DateFormat()
        tickerFlow(300.seconds)
            .map { Date() }
            .distinctUntilChanged { old, new ->
                format.format(old) == format.format(new)
            }
            .onEach { onUpdateDate() }
            .launchIn(viewModelScope)

        actions.filterIsInstance<CrewNameViewAction.UpdateCrewName>()
            .onEach { onUpdateCrewName(it.name) }
            .launchIn(viewModelScope)

        actions.filterIsInstance<CrewNameViewAction.Submit>()
            .onEach { onSubmit() }
            .launchIn(viewModelScope)

        authRepository.getStateFlow()
            .onEach { onAuthRepositoryState(it) }
            .launchIn(viewModelScope)
    }

    private fun onUpdateDate() {
        val format = dateFormatProvider.getReadableDateFormat()
        _state.set { copy(currentDate = format.format(Date())) }
    }

    private fun onAuthRepositoryState(authState: State?) {
        authState?.certificate ?: return

        val stationName = authState.certificate!!.thingName.split("-").last()

        _state.set { copy(stationName = stationName) }
    }

    private fun onUpdateCrewName(name: String) {
        _state.set { copy(crewName = name) }
    }

    private suspend fun onSubmit() {
        val crewNames = state.value.crewName

        crewRepository.saveCrew(crewNames)

        effects.emit(CrewNameViewEffect.Dismiss)
    }

    companion object {
        private val emptyState = CrewNameViewState()
    }

}

internal sealed class CrewNameViewAction : ViewModelAction() {
    object UpdateDate : CrewNameViewAction()
    object Submit : CrewNameViewAction()

    data class UpdateCrewName(val name: String) : CrewNameViewAction()
}

internal sealed class CrewNameViewEffect : ViewModelEffect() {
    object Dismiss : CrewNameViewEffect()
}

internal data class CrewNameViewState(
    val crewName: String = "",
    val stationName: String = "",
    val currentDate: String = "",
) : ViewModelState