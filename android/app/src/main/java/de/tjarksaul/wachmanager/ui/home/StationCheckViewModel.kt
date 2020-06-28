package de.tjarksaul.wachmanager.ui.home

import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tjarksaul.wachmanager.dtos.Entry
import de.tjarksaul.wachmanager.dtos.Field
import de.tjarksaul.wachmanager.dtos.StateKind
import java.util.*

class StationCheckViewModel : ViewModel() {
    fun updateValue(id: String, state: Boolean, stateKind: StateKind?, amount: Int?, note: String?) {
        val arr = _entries.value
        arr?.let {
            val field = it[id]
            field?.let { field ->
                val newEntry = if (state) {
                    Entry(field.entry?.id, field.id, "", state, null, null, null, field.entry?.date ?: "")
                } else {
                    Entry(field.entry?.id, field.id, "", state, stateKind, amount, note, field.entry?.date ?: "")
                }
                it[id] = Field(
                    field.id,
                    field.name,
                    field.parent,
                    field.required,
                    field.note,
                    newEntry
                )
                updateEntries(it)
            }
        }
    }


    fun updateData(list: MutableList<Field>) {
        // todo: sort
        _lastUpdate.value = Date()
        val fields = emptyMap<String, Field>().toMutableMap()
        for (field in list) {
            fields[field.id] = field
        }

        updateEntries(fields)
    }

    fun addEntries(data: MutableList<Entry>) {
        val fields = _entries.value

        if (fields != null) {
            for (entry in data) {
                val field = fields[entry.field]
                if (field != null) {
                    field.entry = entry
                    fields[field.id] = field
                }
            }

            updateEntries(fields)
        }
    }

    fun needsRefresh(): Boolean {
        return _lastUpdate.value == null || !DateUtils.isToday(_lastUpdate.value!!.time)
    }

    private fun updateEntries(entries: MutableMap<String, Field>) {
        _entries.value = entries
        _entryList.value = entries.values.toMutableList()
    }

    private val _lastUpdate = MutableLiveData<Date?>().apply { }

//    val lastUpdate: LiveData<Date?> = _lastUpdate

    private val _entries = MutableLiveData<MutableMap<String, Field>>().apply {
        value = emptyMap<String, Field>().toMutableMap()
    }

    private val _entryList =
        MutableLiveData<MutableList<Field>>().apply { value = emptyList<Field>().toMutableList() }

    val entries: LiveData<MutableList<Field>> = _entryList
}