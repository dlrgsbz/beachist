package de.tjarksaul.wachmanager

import android.content.SharedPreferences
import android.text.format.DateUtils
import de.tjarksaul.wachmanager.ui.base.BaseViewModel
import de.tjarksaul.wachmanager.ui.base.ViewModelAction
import de.tjarksaul.wachmanager.ui.base.ViewModelEffect
import de.tjarksaul.wachmanager.ui.base.ViewModelState

internal class MainViewModel(
    val sharedPreferences: SharedPreferences
) : BaseViewModel<MainViewAction, MainViewState, MainViewEffect>(emptyState) {
    override fun handleActions() {
    }

    fun shouldShowStationSelection(): Boolean {
        val date = sharedPreferences.getLong("lastStationSelectedDate", 0)

        return !DateUtils.isToday(date) || getCrew().trim() == ""
    }

    private fun getCrew(): String {
        return sharedPreferences.getString("crewNames", "") ?: ""
    }

    companion object {
        private val emptyState = MainViewState()
    }
}

internal sealed class MainViewAction : ViewModelAction

internal sealed class MainViewEffect : ViewModelEffect

internal class MainViewState : ViewModelState
