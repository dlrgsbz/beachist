package app.beachist.event.ui

import androidx.lifecycle.viewModelScope
import app.beachist.event.dto.Event
import app.beachist.event.dto.EventType
import app.beachist.event.repository.EventRepository
import app.beachist.shared.NetworkState
import app.beachist.shared.base.FlowBaseViewModel
import app.beachist.shared.base.ViewModelAction
import app.beachist.shared.base.ViewModelEffect
import app.beachist.shared.base.ViewModelState
import app.beachist.shared.base.delay
import app.beachist.shared.base.set
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.*

@ExperimentalCoroutinesApi
internal class EventViewModel(
    private val eventRepository: EventRepository,
) : FlowBaseViewModel<EventListAction, EventListState, EventListEffect>(emptyState) {
    override fun handleActions() {
        actions.filterIsInstance<EventListAction.AddEventClicked>()
            .onEach { onAddEvent() }
            .launchIn(viewModelScope)

        actions.filterIsInstance<EventListAction.SaveEvent>()
            .delay(5000)
            .onEach { addEvent() }
            .launchIn(viewModelScope)

        actions.filterIsInstance<EventListAction.CancelClicked>()
            .onEach { onCancel() }
            .launchIn(viewModelScope)

        actions.filterIsInstance<EventListAction.Refetch>()
            .onEach { onRefetch() }
            .launchIn(viewModelScope)
    }

    private var currentEvent: MutableStateFlow<Event?> = MutableStateFlow(null)

    companion object {
        private val emptyState = EventListState()
    }

    private suspend fun onAddEvent() {
        Timber.tag("EventViewModel").i("onAddEvent")
        val event =
            Event(
                EventType.firstAid,
                UUID.randomUUID().toString(),
                Date(),
                NetworkState.pending,
            )
        _state.set { copy(canAdd = false) }
        currentEvent.value = event

        actions.emit(EventListAction.SaveEvent(event))
    }

    private fun onCancel() {
        _state.set {
            copy(canAdd = true)
        }
        currentEvent.value = null
    }

    private suspend fun addEvent() {
        val event = this.currentEvent.value ?: return

        _state.set { copy(canAdd = true) }

        eventRepository.saveEvent(event)
        currentEvent.value = null
    }

    private fun onRefetch() {
        eventRepository.observeEvents().combine(currentEvent) { list, currentEvent ->
            val items = if (currentEvent != null) list.plus(currentEvent) else list
            _state.set { copy(eventItems = items) }
        }.launchIn(viewModelScope)
    }
}

internal sealed class EventListAction : ViewModelAction() {
    object AddEventClicked : EventListAction()
    object Refetch : EventListAction()
    object CancelClicked : EventListAction()

    data class SaveEvent(val event: Event) : EventListAction()
}

internal sealed class EventListEffect : ViewModelEffect()

internal data class EventListState(
    val eventItems: List<Event> = listOf(),
    val canAdd: Boolean = true,
) : ViewModelState
