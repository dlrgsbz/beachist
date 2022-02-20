package de.tjarksaul.wachmanager.modules.stationCheck

import android.text.format.DateUtils
import androidx.lifecycle.*
import de.tjarksaul.wachmanager.dtos.Entry
import de.tjarksaul.wachmanager.dtos.Field
import de.tjarksaul.wachmanager.dtos.StateKind
import java.util.*

class StationCheckViewModel(private val state: SavedStateHandle) : ViewModel() {
    fun updateValue(
        id: String,
        state: Boolean,
        stateKind: StateKind?,
        amount: Int?,
        note: String?
    ) {
        _entries.value?.let {
            it[id]?.let { field ->
                val newEntry = Entry(
                    field.entry?.id,
                    field.id,
                    "",
                    state,
                    if (state) stateKind else null,
                    if (state) amount else null,
                    if (state) note else null,
                    field.entry?.date ?: ""
                )
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
        val date = Date()
        _lastUpdate.value = date
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
        val value = _lastUpdate.value
        return value == null || !DateUtils.isToday(value.time)
    }

    private fun updateEntries(entries: MutableMap<String, Field>, save: Boolean = true) {
        _entries.value = entries
    }

    fun saveState() {
        _entries.value?.let { state.set("entries", it) }
        _lastUpdate.value?.let { state.set("lastUpdate", it) }
    }

    private val _lastUpdate = state.getLiveData<Date?>("lastUpdate")

    private val _entries = state.getLiveData<MutableMap<String, Field>>("entries")

    private val _entryList = MediatorLiveData<MutableList<Field>>()

    init {
        _entryList.addSource(_entries) { _entryList.value = it.values.toMutableList() }
    }

    val entries: LiveData<MutableList<Field>> = _entryList
}