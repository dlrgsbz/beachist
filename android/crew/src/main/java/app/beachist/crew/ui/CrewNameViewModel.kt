package app.beachist.crew.ui

import app.beachist.auth.repository.AuthRepository
import app.beachist.auth.repository.State
import app.beachist.shared.base.BaseViewModel
import app.beachist.shared.base.ViewModelAction
import app.beachist.shared.base.ViewModelEffect
import app.beachist.shared.base.ViewModelState
import app.beachist.shared.date.DateFormatProvider
import app.beachist.crew.repository.CrewRepository
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import java.util.*
import java.util.concurrent.TimeUnit

internal class CrewNameViewModel(
    private val crewRepository: CrewRepository,
    private val authRepository: AuthRepository,
    private val dateFormatProvider: DateFormatProvider,
) : BaseViewModel<CrewNameViewAction, CrewNameViewState, CrewNameViewEffect>(emptyState) {
    override fun handleActions() {
        disposables += actions.ofType<CrewNameViewAction.UpdateDate>()
            .switchMapWithDateUpdate()
            .subscribe { onUpdateDate() }

        disposables += actions.ofType<CrewNameViewAction.UpdateCrewName>()
            .subscribe { onUpdateCrewName(it.name) }

        disposables += actions.ofType<CrewNameViewAction.Submit>()
            .subscribe { onSubmit() }

        disposables += authRepository.getState()
            .subscribe { onAuthRepositoryState(it) }
    }

    private fun <T> Observable<T>.switchMapWithDateUpdate() = this.switchMap {
        actions.ofType<CrewNameViewAction.UpdateDate>()
            .startWith(CrewNameViewAction.UpdateDate)
    }.debounce(300, TimeUnit.MILLISECONDS)

    private fun onUpdateDate() {
        val format = dateFormatProvider.getReadableDateFormat()
        state.set { copy(currentDate = format.format(Date())) }
    }

    private fun onAuthRepositoryState(authState: State?) {
        authState?.certificate ?: return

        val stationName = authState.certificate!!.thingName.split("-").last()

        state.set { copy(stationName = stationName) }
    }

    private fun onUpdateCrewName(name: String) {
        state.set { copy(crewName = name) }
    }

    private fun onSubmit() = state.get {
        val crewNames = it.crewName

        crewRepository.saveCrew(crewNames)

        effects.onNext(CrewNameViewEffect.Dismiss)
    }

    companion object {
        private val emptyState = CrewNameViewState()
    }

}

internal sealed class CrewNameViewAction : ViewModelAction {
    object UpdateDate : CrewNameViewAction()
    object Submit: CrewNameViewAction()

    data class UpdateCrewName(val name: String) : CrewNameViewAction()
}

internal sealed class CrewNameViewEffect : ViewModelEffect {
    object Dismiss : CrewNameViewEffect()
}

internal data class CrewNameViewState(
    val crewName: String = "",
    val stationName: String = "",
    val currentDate: String = "",
) : ViewModelState