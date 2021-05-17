package de.tjarksaul.wachmanager.modules.main

import android.content.SharedPreferences
import android.text.format.DateUtils
import de.tjarksaul.wachmanager.modules.base.BaseViewModel
import de.tjarksaul.wachmanager.modules.base.ViewModelAction
import de.tjarksaul.wachmanager.modules.base.ViewModelEffect
import de.tjarksaul.wachmanager.modules.base.ViewModelState
import de.tjarksaul.wachmanager.repositories.StationRepository

internal class MainViewModel(
    val stationRepository: StationRepository
) : BaseViewModel<MainViewAction, MainViewState, MainViewEffect>(
    emptyState
) {
    override fun handleActions() {
    }

    fun shouldShowStationSelection(): Boolean {
        val date = stationRepository.getLastUpdateDate()

        return !DateUtils.isToday(date) || stationRepository.getCrew().trim() == ""
    }

    companion object {
        private val emptyState =
            MainViewState()
    }
}

internal sealed class MainViewAction : ViewModelAction

internal sealed class MainViewEffect : ViewModelEffect

internal class MainViewState : ViewModelState
