package de.tjarksaul.wachmanager.ui.specialEvents

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tjarksaul.wachmanager.dtos.NetworkState
import de.tjarksaul.wachmanager.dtos.SpecialEvent
import de.tjarksaul.wachmanager.dtos.SpecialEventKind
import java.text.SimpleDateFormat
import java.util.*

class SpecialEventsViewModel : ViewModel() {
    fun updateData(data: MutableList<SpecialEvent>) {
        _events.value = data
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

    fun getDate(): String {
        val simpleDateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        return simpleDateFormat.format(Date())
    }

    private var iterator: Int = 1

    private val _events = MutableLiveData<MutableList<SpecialEvent>>().apply {
        value = emptyList<SpecialEvent>().toMutableList()
    }
    val events: LiveData<MutableList<SpecialEvent>> = _events
}