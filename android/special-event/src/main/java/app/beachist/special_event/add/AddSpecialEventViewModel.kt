package app.beachist.special_event.add

import app.beachist.shared.base.BaseViewModel
import app.beachist.shared.base.ViewModelAction
import app.beachist.shared.base.ViewModelEffect
import app.beachist.shared.base.ViewModelState
import app.beachist.special_event.R
import app.beachist.special_event.dtos.SpecialEventKind
import app.beachist.special_event.service.SpecialEventService
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
        val error =
            if (title.isBlank() || title.length < 8) ErrorState.HasError(R.string.special_event_title_error_length) else ErrorState.NoError
        state.set { copy(title = title, titleError = error) }
        actions.onNext(AddSpecialEventAction.CheckCompleted)
    }

    private fun onNotifierUpdated(it: String) {
        val notifier = it.trim()
        val error =
            if (notifier.isBlank() || notifier.length < 2) ErrorState.HasError(R.string.special_event_notifier_error_length) else ErrorState.NoError
        state.set { copy(notifier = notifier, notifierError = error) }
        actions.onNext(AddSpecialEventAction.CheckCompleted)
    }

    private fun onNotesUpdated(it: String) {
        val notes = it.trim()
        val error =
            if (notes.isBlank() || notes.length < 8) ErrorState.HasError(R.string.special_event_notes_error_length) else ErrorState.NoError
        state.set { copy(note = notes, noteError = error) }
        actions.onNext(AddSpecialEventAction.CheckCompleted)
    }

    private fun onKindUpdated(kind: SpecialEventKind?) {
        val error =
            kind?.let { ErrorState.NoError } ?: ErrorState.HasError(R.string.special_event_kind_error_none_selected)
        state.set { copy(kind = kind, kindError = error) }
        actions.onNext(AddSpecialEventAction.CheckCompleted)
    }

    private fun onCheckCompleted() {
        state.set {
            val enabled = title.isNotBlank() && note.isNotBlank() && notifier.isNotBlank() && kind !== null
                    && titleError == ErrorState.NoError && noteError == ErrorState.NoError && notifierError == ErrorState.NoError && kindError == ErrorState.NoError
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

internal sealed class AddSpecialEventAction : ViewModelAction() {
    object SaveSpecialEvent : AddSpecialEventAction()
    object CheckCompleted : AddSpecialEventAction()

    data class TitleUpdated(val title: String) : AddSpecialEventAction()
    data class NotifierUpdated(val notifier: String) : AddSpecialEventAction()
    data class NotesUpdated(val notes: String) : AddSpecialEventAction()
    data class KindUpdated(val kind: SpecialEventKind?) : AddSpecialEventAction()
}

internal sealed class AddSpecialEventEffect : ViewModelEffect() {
    object PopView : AddSpecialEventEffect()
    object HideKeyboard : AddSpecialEventEffect()
    object IncompleteError : AddSpecialEventEffect()
}

internal data class AddSpecialEventState(
    val title: String = "",
    val titleError: ErrorState = ErrorState.NoError,
    val note: String = "",
    val noteError: ErrorState = ErrorState.NoError,
    val notifier: String = "",
    val notifierError: ErrorState = ErrorState.NoError,
    val kind: SpecialEventKind? = null,
    val kindError: ErrorState = ErrorState.NoError,
    val saveButtonEnabled: Boolean = false,
) : ViewModelState

internal sealed class ErrorState {
    object NoError : ErrorState()
    data class HasError(val error: Int) : ErrorState()
}
