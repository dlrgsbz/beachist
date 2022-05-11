package de.tjarksaul.wachmanager.modules.events

import de.tjarksaul.wachmanager.dtos.EventType
import de.tjarksaul.wachmanager.dtos.NetworkState
import de.tjarksaul.wachmanager.modules.base.BaseViewModel
import de.tjarksaul.wachmanager.modules.base.ViewModelAction
import de.tjarksaul.wachmanager.modules.base.ViewModelEffect
import de.tjarksaul.wachmanager.modules.base.ViewModelState
import de.tjarksaul.wachmanager.repositories.EventRepository
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import java.util.*
import java.util.concurrent.TimeUnit

internal class EventViewModel(
    private val eventRepository: EventRepository,
    private val eventBackendRepository: EventBackendRepository,
) : BaseViewModel<EventListAction, EventListState, EventListEffect>(emptyState) {
    override fun handleActions() {
        disposables += actions.ofType<EventListAction.AddEventClicked>()
            .subscribe { onAddEvent() }

        disposables += actions.ofType<EventListAction.SaveEvent>()
            .delay(5, TimeUnit.SECONDS)
            .subscribe { addEvent(it.event) }

        disposables += actions.ofType<EventListAction.CancelClicked>()
            .subscribe { onCancel() }

        disposables += actions.ofType<EventListAction.Refetch>()
            .subscribe { onRefetch() }

        disposables += eventBackendRepository.observeEventUpdates()
            .subscribe { onEventSuccessResponse(it) }
    }

    companion object {
        private val emptyState = EventListState()
    }

    private fun onAddEvent() {
        val event =
            Event(
                EventType.firstAid,
                UUID.randomUUID().toString(),
                Date(),
                NetworkState.pending
            )
        state.set {
            val events: MutableList<Event> = this.eventItems.toMutableList()
            events.add(event)
            return@set copy(eventItems = events, currentEvent = event, canAdd = false)
        }

        actions.onNext(EventListAction.SaveEvent(event))
    }

    private fun reset(event: Event) {
        state.get { current ->
            val events = current.eventItems.toMutableList()
            val index = events.indexOfFirst { it.id == event.id }
            if (index != -1) {
                events.removeAt(index)
            }
            state.set {
                copy(
                    canAdd = true,
                    cancelled = false,
                    currentEvent = null,
                    eventItems = events
                )
            }
        }
    }

    private fun onCancel() {
        state.get { current ->
            if (current.currentEvent == null) {
                return@get
            }

            reset(current.currentEvent)
            state.set { copy(cancelled = true) }
        }
    }

    private fun addEvent(event: Event) {
        state.get { current ->
            if (current.cancelled) {
                reset(event)
                return@get
            }

            state.set { copy(canAdd = true, cancelled = false) }

            eventRepository.saveEvent(event)
            saveEvent(event)
        }
    }

    private fun onRefetch() {
        disposables += eventRepository.getEvents()
            .subscribe { result ->
                state.set { copy(eventItems = result) }
                result.map {
                    if (it.state == NetworkState.pending) {
                        saveEvent(it)
                    }
                }
            }
    }

    private fun saveEvent(event: Event) {
        eventBackendRepository.createEvent(event.type, event.id, event.dateString)
    }

    private fun onEventSuccessResponse(id: String) {
        state.set {
            val events = this.eventItems.toMutableList()
            val index = events.indexOfFirst { it.id == id }
            if (index >= 0) {
                val newEvent = events[index].copy(state = NetworkState.successful)
                events[index] = newEvent
                eventRepository.updateEvent(newEvent)
            }
            return@set copy(eventItems = events)
        }
    }
}

internal sealed class EventListAction : ViewModelAction {
    object AddEventClicked : EventListAction()
    object Refetch : EventListAction()
    object CancelClicked : EventListAction()

    data class SaveEvent(val event: Event) : EventListAction()
}

internal sealed class EventListEffect : ViewModelEffect

internal data class EventListState(
    val eventItems: List<Event> = listOf(),
    val currentEvent: Event? = null,
    val canAdd: Boolean = true,
    val cancelled: Boolean = false,
) : ViewModelState
