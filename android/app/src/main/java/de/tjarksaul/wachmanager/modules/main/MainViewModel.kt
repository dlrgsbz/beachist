package de.tjarksaul.wachmanager.modules.main

import android.text.format.DateUtils
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
    val stationRepository: StationRepository
) : BaseViewModel<MainViewAction, MainViewState, MainViewEffect>(
    emptyState
) {
    override fun handleActions() {
        disposables += Observable.interval(1000L, TimeUnit.MILLISECONDS).timeInterval()
            .subscribe { actions.onNext(MainViewAction.CheckStationSelection) }

        disposables += actions.ofType<MainViewAction.CheckStationSelection>()
            .subscribe { onCheckStationSelection() }
    }

    private fun onCheckStationSelection() {
        val date = stationRepository.getLastUpdateDate()

        val shouldShowStationSelection = !DateUtils.isToday(date) || stationRepository.getCrew().trim() == ""

        state.set { copy(shouldShowStationSelection = shouldShowStationSelection) }
    }

    companion object {
        private val emptyState =
            MainViewState()
    }
}

internal sealed class MainViewAction : ViewModelAction {
    object CheckStationSelection : MainViewAction()
}

internal sealed class MainViewEffect : ViewModelEffect

internal data class MainViewState(
    val shouldShowStationSelection: Boolean = false
) : ViewModelState
