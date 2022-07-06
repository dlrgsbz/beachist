package app.beachist.station_check.ui

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import app.beachist.shared.base.BaseViewModel
import app.beachist.shared.base.RxViewState
import app.beachist.shared.base.ViewModelAction
import app.beachist.shared.base.ViewModelEffect
import app.beachist.shared.base.ViewModelState
import app.beachist.station_check.dtos.Entry
import app.beachist.station_check.dtos.Field
import app.beachist.station_check.dtos.StateKind
import app.beachist.station_check.repository.FieldLocalRepository
import app.beachist.station_check.service.EntryService
import app.beachist.station_check.service.FieldService
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.parcel.Parcelize
import timber.log.Timber
import java.util.*
import kotlin.concurrent.schedule

internal class StationCheckViewModel(
    private val fieldService: FieldService,
    private val entryService: EntryService,
    private val fieldRepo: FieldLocalRepository,
    private val handle: SavedStateHandle
) : BaseViewModel<StationCheckAction, StationCheckState, StationCheckEffect>(
    handle.get("StationCheckState") ?: emptyState
) {
    companion object {
        private val emptyState = StationCheckState()
    }

    override fun handleActions() {
        disposables += actions.ofType<StationCheckAction.UpdateValue>()
            .subscribe { onUpdateValue(it.id, it.state, it.stateKind, it.amount, it.note) }

        disposables += actions.ofType<StationCheckAction.MarkItemOkay>()
            .subscribe { onMarkItemOkay(it.id) }

        disposables += actions.ofType<StationCheckAction.MarkItemNotOkay>()
            .subscribe { onMarkItemNotOkay(it.id) }

        disposables += actions.ofType<StationCheckAction.MarkItemBroken>()
            .subscribe { onMarkItemBroken(it.id) }

        disposables += actions.ofType<StationCheckAction.MarkItemTooLittle>()
            .subscribe { onMarkItemTooLittle(it.id) }

        disposables += actions.ofType<StationCheckAction.MarkItemOther>()
            .subscribe { onMarkItemOther(it.id) }

        disposables += actions.ofType<StationCheckAction.AddItemAmount>()
            .subscribe { onAddItemAmount(it.id, it.amount) }

        disposables += actions.ofType<StationCheckAction.AddItemNote>()
            .subscribe { onAddItemNote(it.id, it.note) }

        disposables += actions.ofType<StationCheckAction.Refresh>()
            .subscribe { fetchData() }

        Timber.tag("StationCheckViewModel").d("handleActions")
        disposables += fieldService.fieldsObservable
            .subscribe { onFieldResponse(it) }
    }

    private fun onMarkItemOkay(id: String) {
        val event = StationCheckAction.UpdateValue(id, true, null, null, null)
        actions.onNext(event)
    }

    private fun onMarkItemNotOkay(id: String) {
        val event = StationCheckAction.UpdateValue(id, false, null, null, null)
        actions.onNext(event)
    }

    private fun onMarkItemBroken(id: String) {
        val event = StationCheckAction.UpdateValue(id, false, StateKind.broken, null, null)
        actions.onNext(event)
        val effect = StationCheckEffect.ShowNoteBox(id)
        effects.onNext(effect)
    }

    private fun onMarkItemTooLittle(id: String) {
        state.withFieldId(id) { field ->
            // if there's only 1 required we go straight to sending 0
            val showAmountBox = (field.required ?: 0) > 1
            val event = if (!showAmountBox)
                StationCheckAction.UpdateValue(id, false, StateKind.tooLittle, 0, null)
            else
                StationCheckAction.UpdateValue(id, false, StateKind.tooLittle, null, null)
            actions.onNext(event)
            if (!showAmountBox) {
                return@withFieldId
            }

            val effect = StationCheckEffect.ShowAmountBox(id)
            effects.onNext(effect)
        }
    }

    private fun onMarkItemOther(id: String) {
        val event = StationCheckAction.UpdateValue(id, false, StateKind.other, null, null)
        actions.onNext(event)
        val effect = StationCheckEffect.ShowNoteBox(id)
        effects.onNext(effect)
    }

    private fun onAddItemAmount(id: String, amount: Int) {
        val event = StationCheckAction.UpdateValue(id, false, StateKind.tooLittle, amount, null)
        actions.onNext(event)
    }

    private fun onAddItemNote(id: String, note: String) {
        state.withFieldId(id) { field ->
            val stateKind = field.entry?.stateKind ?: StateKind.other
            val event = StationCheckAction.UpdateValue(id, false, stateKind, null, note)
            actions.onNext(event)
        }
    }

    private fun onUpdateValue(
        id: String,
        valueState: Boolean,
        stateKind: StateKind?,
        amount: Int?,
        note: String?
    ) {
        state.set {
            var list = this.entries

            val entryIdx = list.indexOfFirst { it.id == id }

            if (entryIdx < 0) {
                return@set this
            }

            val field = list[entryIdx]

            val newEntry = Entry(
                field.entry?.id,
                field.id,
                "",
                valueState,
                if (!valueState) stateKind else null,
                if (!valueState) amount else null,
                if (!valueState) note else null,
                field.entry?.date ?: ""
            )

            list = list.toMutableList()
            list[entryIdx] = field.copy(entry = newEntry)

            return@set copy(entries = list)
        }

        if (isIncomplete(valueState, stateKind, amount, note)) {
            return
        }
        entryService.updateEntry(
            id,
            valueState,
            stateKind,
            amount,
            note
        )
    }

    private fun isIncomplete(
        state: Boolean,
        stateKind: StateKind?,
        amount: Int?,
        note: String?
    ): Boolean {
        if (!state) {
            // make sure that we don't submit incomplete state
            if (stateKind == null) {
                return true
            }
            if (stateKind == StateKind.tooLittle && amount == null) {
                return true
            }
            if (stateKind != StateKind.tooLittle && note == null) {
                return true
            }
        }
        return false
    }

    private var shouldFetchLocally = false
    private var timer: Timer? = null

    private fun fetchData() {
        Timber.tag("StationCheckViewModel").i("Fetching data")
        shouldFetchLocally = true

        // as backup: get data from local data
        timer = Timer("FETCH_LOCAL", false)
        timer?.schedule(10_000) {
            fetchFromLocalCache()
        }

        fieldService.getFieldsWithEntries()
    }

    private fun fetchFromLocalCache() {
        if (!shouldFetchLocally) {
            return
        }

        // fetch
        val data = fieldRepo.getCachedFields()
        if (!shouldFetchLocally) {
            return
        }

        Timber.tag("StationCheckViewModel").i("Setting fields from local cache")

        state.set {
            copy(entries = data)
        }
    }

    private fun onFieldResponse(data: List<Field>) {
        if (data.isEmpty()) {
            Timber.tag("StationCheckViewModel").i("Got empty response from backend")
            return
        }

        Timber.tag("StationCheckViewModel").i("Got %d fields from backend", data.size)

        timer?.cancel()
        shouldFetchLocally = false

        state.set {
            copy(entries = data, lastUpdated = Date())
        }
    }

    fun saveState() {
        state.get {
            handle.set("StationCheckState", it)
        }
    }
}

internal sealed class StationCheckAction : ViewModelAction {
    object Refresh : StationCheckAction()
    data class MarkItemOkay(val id: String) : StationCheckAction()
    data class MarkItemNotOkay(val id: String) : StationCheckAction()
    data class MarkItemBroken(val id: String) : StationCheckAction()
    data class MarkItemTooLittle(val id: String) : StationCheckAction()
    data class MarkItemOther(val id: String) : StationCheckAction()
    data class AddItemAmount(val id: String, val amount: Int) : StationCheckAction()
    data class AddItemNote(val id: String, val note: String) : StationCheckAction()
    data class UpdateValue(
        val id: String,
        val state: Boolean,
        val stateKind: StateKind?,
        val amount: Int?,
        val note: String?
    ) : StationCheckAction()
}

internal sealed class StationCheckEffect : ViewModelEffect {
    data class ShowAmountBox(val id: String) : StationCheckEffect()
    data class ShowNoteBox(val id: String) : StationCheckEffect()
}

@Parcelize
internal data class StationCheckState(
    val entries: List<Field> = listOf(),
    val lastUpdated: Date? = null
) : ViewModelState, Parcelable

private fun RxViewState<StationCheckState>.withFieldId(id: String, callback: (Field) -> Unit) {
    this.get { state ->
        val list = state.entries

        val index = list.indexOfFirst { it.id == id }

        if (index < 0) {
            return@get
        }

        val field = list[index]

        callback(field)
    }
}
