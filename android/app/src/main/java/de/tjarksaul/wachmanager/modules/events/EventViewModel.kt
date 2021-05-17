package de.tjarksaul.wachmanager.modules.events

import androidx.lifecycle.MutableLiveData
import de.tjarksaul.wachmanager.GlobalStore
import de.tjarksaul.wachmanager.dtos.EventStats
import de.tjarksaul.wachmanager.modules.base.BaseViewModel
import de.tjarksaul.wachmanager.modules.base.ViewModelAction
import de.tjarksaul.wachmanager.modules.base.ViewModelEffect
import de.tjarksaul.wachmanager.modules.base.ViewModelState

internal class EventViewModel(
    val globalStore: GlobalStore
) : BaseViewModel<EventListAction, EventListState, EventListEffect>(emptyState) {
    fun updateData(data: EventStats) {
        _firstAid.value = data.firstAid
    }

    private val _firstAid = MutableLiveData<Int>().apply {
        value = 0
    }

    override fun handleActions() {
    }

    companion object {
        private val emptyState = EventListState()
    }
}

internal sealed class EventListAction : ViewModelAction

internal sealed class EventListEffect : ViewModelEffect

internal data class EventListState(
    val eventItems: List<Event> = listOf(),
    val currentEvent: Event? = null,
    val canAdd: Boolean = true,
    val cancelled: Boolean = false
) : ViewModelState
