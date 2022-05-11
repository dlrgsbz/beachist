package de.tjarksaul.wachmanager.modules.specialEvents

import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.dtos.SpecialEventKind
import de.tjarksaul.wachmanager.modules.base.BaseViewModel
import de.tjarksaul.wachmanager.modules.base.ViewModelAction
import de.tjarksaul.wachmanager.modules.base.ViewModelEffect
import de.tjarksaul.wachmanager.modules.base.ViewModelState
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign

internal class AddSpecialEventViewModel(
    private val specialEventService: SpecialEventService,
) : BaseViewModel<AddSpecialEventAction, AddSpecialEventState, AddSpecialEventEffect>(emptyState) {
    companion object {
        private val emptyState = AddSpecialEventState()
    }

    override fun handleActions() {
        disposables += actions.ofType<AddSpecialEventAction.TitleUpdated>()
            .subscribe { onTitleUpdated(it.title) }

        disposables += actions.ofType<AddSpecialEventAction.NotifierUpdated>()
            .subscribe { onNotifierUpdated(it.notifier) }

        disposables += actions.ofType<AddSpecialEventAction.NotesUpdated>()
            .subscribe { onNotesUpdated(it.notes) }

        disposables += actions.ofType<AddSpecialEventAction.KindUpdated>()
            .subscribe { onKindUpdated(it.kind) }

        disposables += actions.ofType<AddSpecialEventAction.CheckCompleted>()
            .subscribe { onCheckCompleted() }

        disposables += actions.ofType<AddSpecialEventAction.SaveSpecialEvent>()
            .subscribe { onSave() }
    }

    private fun onTitleUpdated(it: String) {
        val title = it.trim()
        val error = if (title.isBlank() || title.length < 8) R.string.special_event_title_error_length else null
        state.set { copy(title = title, titleError = error) }
    }

    private fun onNotifierUpdated(it: String) {
        val notifier = it.trim()
        val error = if (notifier.isBlank() || notifier.length < 2) R.string.special_event_notifier_error_length else null
        state.set { copy(notifier = notifier, notifierError = error) }
    }

    private fun onNotesUpdated(it: String) {
        val notes = it.trim()
        val error = if (notes.isBlank() || notes.length < 8) R.string.special_event_notes_error_length else null
        state.set { copy(note = notes, noteError = error) }
    }

    private fun onKindUpdated(kind: SpecialEventKind?) {
        val error = kind?.let { null } ?: R.string.special_event_kind_error_none_selected
        state.set { copy(kind = kind, kindError = error) }
    }

    private fun onCheckCompleted() {
        state.set {
            val enabled = title.isNotBlank() && note.isNotBlank() && notifier.isNotBlank() && kind !== null
                    && titleError == null && noteError == null && notifierError == null && kindError == null
            return@set copy(saveButtonEnabled = enabled)
        }
    }

    private fun onSave() {
        effects.onNext(AddSpecialEventEffect.HideKeyboard)
        state.get {
            if (!it.saveButtonEnabled) {
                effects.onNext(AddSpecialEventEffect.IncompleteError)
                return@get
            }

            specialEventService.createSpecialEvent(it.title, it.note, it.notifier, it.kind!!)
            effects.onNext(AddSpecialEventEffect.PopView)
        }
    }
}

internal sealed class AddSpecialEventAction : ViewModelAction {
    object SaveSpecialEvent: AddSpecialEventAction()
    object CheckCompleted: AddSpecialEventAction()

    data class TitleUpdated(val title: String): AddSpecialEventAction()
    data class NotifierUpdated(val notifier: String): AddSpecialEventAction()
    data class NotesUpdated(val notes: String) : AddSpecialEventAction()
    data class KindUpdated(val kind: SpecialEventKind?): AddSpecialEventAction()
}

internal sealed class AddSpecialEventEffect : ViewModelEffect {
    object PopView: AddSpecialEventEffect()
    object HideKeyboard: AddSpecialEventEffect()
    object IncompleteError: AddSpecialEventEffect()
}

internal data class AddSpecialEventState(
    val title: String = "",
    val titleError: Int? = null,
    val note: String = "",
    val noteError: Int? = null,
    val notifier: String = "",
    val notifierError: Int? = null,
    val kind: SpecialEventKind? = null,
    val kindError: Int? = null,
    val saveButtonEnabled: Boolean = false,
) : ViewModelState
