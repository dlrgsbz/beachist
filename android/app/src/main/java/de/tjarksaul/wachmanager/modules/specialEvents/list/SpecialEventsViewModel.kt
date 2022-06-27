package de.tjarksaul.wachmanager.modules.specialEvents.list

import androidx.lifecycle.viewModelScope
import de.tjarksaul.wachmanager.dtos.SpecialEvent
import app.beachist.shared.base.BaseViewModel
import app.beachist.shared.base.ViewModelAction
import app.beachist.shared.base.ViewModelEffect
import app.beachist.shared.base.ViewModelState
import de.tjarksaul.wachmanager.modules.specialEvents.repository.SpecialEventRepository
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import java.util.*

@ExperimentalCoroutinesApi
internal class SpecialEventsViewModel(private val repository: SpecialEventRepository) :
    BaseViewModel<SpecialEventListAction, SpecialEventListState, SpecialEventListEffect>(emptyState) {

    companion object {
        private val emptyState = SpecialEventListState()
    }

    override fun handleActions() {
        disposables += actions.ofType<SpecialEventListAction.CreateEvent>()
            .subscribe { onCreateSpecialEvent() }

        observeItems()
    }

    private fun onCreateSpecialEvent() {
        effects.onNext(SpecialEventListEffect.ShowCreateEventView)
    }

    private fun observeItems() {
        repository.getSpecialEvents(Date()).mapLatest {
            state.set { copy(eventItems = it) }
        }.launchIn(viewModelScope)
    }
}

internal sealed class SpecialEventListAction : ViewModelAction {
    object CreateEvent : SpecialEventListAction()
}

internal sealed class SpecialEventListEffect : ViewModelEffect {
    object ShowCreateEventView : SpecialEventListEffect()
}

internal data class SpecialEventListState(
    val eventItems: List<SpecialEvent> = listOf(),
) : ViewModelState
