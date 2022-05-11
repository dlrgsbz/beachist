package de.tjarksaul.wachmanager.modules.specialEvents

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.tjarksaul.wachmanager.dtos.NetworkState
import de.tjarksaul.wachmanager.dtos.SpecialEvent
import de.tjarksaul.wachmanager.dtos.SpecialEventKind
import de.tjarksaul.wachmanager.modules.base.BaseViewModel
import de.tjarksaul.wachmanager.modules.base.ViewModelAction
import de.tjarksaul.wachmanager.modules.base.ViewModelEffect
import de.tjarksaul.wachmanager.modules.base.ViewModelState
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import java.text.SimpleDateFormat
import java.util.*

internal class SpecialEventsViewModel :
    BaseViewModel<SpecialEventListAction, SpecialEventListState, SpecialEventListEffect>(emptyState) {
    companion object {
        private val emptyState = SpecialEventListState()
    }

    override fun handleActions() {
        disposables += actions.ofType<SpecialEventListAction.CreateEvent>()
            .subscribe { onCreateSpecialEvent() }

        disposables += actions.ofType<SpecialEventListAction.Refetch>()
            .subscribe { onRefetch() }

        // todo: subscribe to updates
    }

    private fun onCreateSpecialEvent() {
        // todo: push to add view
    }

    private fun onRefetch() {
        // todo: send mqtt message to get all events
    }

    fun addEntry(title: String, note: String, notifier: String, kind: SpecialEventKind): String {
        val id = UUID.randomUUID().toString()
        val list = _events.value
        list?.let {
            it.add(
                SpecialEvent(
                    id,
                    title,
                    note,
                    notifier,
                    getDate(),
                    kind,
                    networkState = NetworkState.pending
                )
            )
            _events.value = it
        }
        return id
    }

    fun updateEntryFromNetwork(id: String, newId: String, networkState: NetworkState) {
        val list = _events.value
        list?.let {
            val index = it.indexOfFirst { elem -> elem.id == id }
            if (index >= 0) {
                val item = it[index]
                it[index] = item.copy(id = newId, networkState = networkState)
            }
            _events.value = it
        }
    }

    private fun getDate(): String {
        val simpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        return simpleDateFormat.format(Date())
    }

    private val _events = MutableLiveData<MutableList<SpecialEvent>>().apply {
        value = emptyList<SpecialEvent>().toMutableList()
    }
}

internal sealed class SpecialEventListAction : ViewModelAction {
    object Refetch : SpecialEventListAction()
    object CreateEvent : SpecialEventListAction()
}

internal sealed class SpecialEventListEffect : ViewModelEffect {
    object ShowCreateEventView : SpecialEventListEffect()
}

internal data class SpecialEventListState(
    val eventItems: List<SpecialEvent> = listOf(),
) : ViewModelState
