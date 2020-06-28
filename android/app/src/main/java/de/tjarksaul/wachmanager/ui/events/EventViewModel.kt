package de.tjarksaul.wachmanager.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tjarksaul.wachmanager.dtos.EventStats

class EventViewModel : ViewModel() {
    fun updateData(data: EventStats) {
        _firstAid.value = data.firstAid
    }

    fun increment() {
        _firstAid.value = _firstAid.value?.plus(1)
    }

    fun decrement() {
        val minus = _firstAid.value?.minus(1)
        _firstAid.value = if (minus != null && minus > 0) minus else 0
    }

    private val _firstAid = MutableLiveData<Int>().apply {
        value = 0
    }

    val firstAid: LiveData<Int> = _firstAid
}