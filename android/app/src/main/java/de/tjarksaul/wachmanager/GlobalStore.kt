package de.tjarksaul.wachmanager

import de.tjarksaul.wachmanager.api.Async
import de.tjarksaul.wachmanager.dtos.EventType
import de.tjarksaul.wachmanager.dtos.NetworkState
import de.tjarksaul.wachmanager.repositories.EventRepository
import de.tjarksaul.wachmanager.repositories.StationRepository
import de.tjarksaul.wachmanager.ui.base.RxViewState
import de.tjarksaul.wachmanager.ui.base.mapNotNull
import de.tjarksaul.wachmanager.ui.events.CreateEventUseCase
import de.tjarksaul.wachmanager.ui.events.Event
import de.tjarksaul.wachmanager.ui.events.PostEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class GlobalStore(
    private val createEvent: CreateEventUseCase,
    private val stationRepository: StationRepository,
    private val eventRepository: EventRepository
) {
    private val state = RxViewState(emptyState)

    private val disposables = CompositeDisposable()

    private val actions: PublishSubject<GlobalAction> = PublishSubject.create<GlobalAction>()

    private fun handleActions() {
        disposables += actions.ofType<GlobalAction.AddEventClicked>()
            .subscribe { onAddEvent() }

        disposables += actions.ofType<GlobalAction.SaveEvent>()
            .delay(5, TimeUnit.SECONDS)
            .subscribe { addEvent(it.event) }

        disposables += actions.ofType<GlobalAction.CancelClicked>()
            .subscribe { onCancel() }

        disposables += actions.ofType<GlobalAction.Refetch>()
            .subscribe { onRefetch() }
    }

    init {
        handleActions()
    }

    fun attach(actions: Observable<GlobalAction>) {
//        disposables.clear()

        //Route actions to the internal actions.
        disposables += actions
            .doOnNext {
                Timber.tag(this.javaClass.simpleName)
                Timber.d("$it")
            }
            .subscribe { this.actions.onNext(it) }
    }

    private fun <T> Observable<T>.switchMapWithRefetch() = this.switchMap {
        actions.ofType<GlobalAction.Refetch>()
            .startWith(GlobalAction.Refetch)
    }.debounce(300, TimeUnit.MILLISECONDS)

    companion object {
        private val emptyState = GlobalState()
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

        actions.onNext(GlobalAction.SaveEvent(event))
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
        // todo: persist events to db
        state.get { current ->
            if (current.cancelled) {
                reset(event)
                return@get
            }

            state.set { copy(canAdd = true, cancelled = false) }

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
        val stationId = stationRepository.getStationId()
        disposables += createEvent(
            stationId,
            PostEvent(event.type, event.id, event.dateString)
        )
            .subscribe { result ->
                when (result) {
                    is Async.Success -> state.set {
                        val events = this.eventItems.toMutableList()
                        val index = events.indexOfFirst { it.id == event.id }
                        if (index >= 0) {
                            val newEvent = events[index].copy(state = NetworkState.successful)
                            events[index] = newEvent
                            eventRepository.updateEvent(newEvent)
                        }
                        return@set copy(eventItems = events)
                    }
                    is Async.Failure -> { /* todo: do something here */
                    }
                }
            }
    }

    fun <T> stateOf(extractor: GlobalState.() -> T): Observable<T> = state.observable
        .stateOf(extractor)
        .distinctUntilChanged()
        .observeOnMainThread()
}

data class GlobalState(
    val eventItems: List<Event> = listOf(),
    val currentEvent: Event? = null,
    val canAdd: Boolean = true,
    val cancelled: Boolean = false
)

sealed class GlobalAction {
    object AddEventClicked : GlobalAction()
    object Refetch : GlobalAction()
    object CancelClicked : GlobalAction()

    data class SaveEvent(val event: Event) : GlobalAction()
}

/**
 * Create a stateOf observer for ViewModel variables.
 * A stateOf can be simply created by using the lambda syntax for values in a class: Class::stateOf.
 */
private fun <T> Observable<GlobalState>.stateOf(extractor: GlobalState.() -> T) =
    this.mapNotNull { extractor(it) }.distinctUntilChanged()

/**
 * Schedules the subscription to be called on the Main Thread.
 */
private fun <T> Observable<T>.observeOnMainThread() = this.observeOn(AndroidSchedulers.mainThread())