package de.tjarksaul.wachmanager.modules.specialEvents

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.dtos.SpecialEventKind
import de.tjarksaul.wachmanager.modules.base.BaseFragment
import de.tjarksaul.wachmanager.util.StackName
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_add_special_event.*
import org.koin.androidx.viewmodel.ext.android.viewModel


internal class AddSpecialEventFragment : BaseFragment() {
    private val disposables = CompositeDisposable()

    private val viewModel: AddSpecialEventViewModel by viewModel()

    private val actions: PublishSubject<AddSpecialEventAction> = PublishSubject.create()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_add_special_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupBindings()
    }

    private fun setupView() {
        textInputLayoutSpecialTitle.isHintEnabled = true
        textInputLayoutSpecialTitle.hint = getString(R.string.special_event_title_hint)

        textInputLayoutSpecialNotifier.isHintEnabled = true
        textInputLayoutSpecialNotifier.hint = getString(R.string.special_event_notifier_hint)
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        disposables += viewModel.stateOf { titleError }
            .subscribe { onTitleErrorChanged(it) }

        disposables += viewModel.stateOf { notifierError }
            .subscribe { onNotifierErrorChanged(it) }

        disposables += viewModel.stateOf { kindError }
            .subscribe { onKindErrorChanged(it) }

        disposables += viewModel.stateOf { saveButtonEnabled }
            .subscribe { saveSpecialEventButton.isEnabled = it }

        disposables += viewModel.effects()
            .ofType<AddSpecialEventEffect.PopView>()
            .subscribe { onPopView() }

        disposables += viewModel.effects()
            .ofType<AddSpecialEventEffect.IncompleteError>()
            .subscribe { onIncompleteError() }

        disposables += viewModel.effects()
            .ofType<AddSpecialEventEffect.HideKeyboard>()
            .subscribe { onHideKeyboard() }

        editTextTextSpecialTitle.doAfterTextChanged {
            actions.onNext(AddSpecialEventAction.TitleUpdated(it.toString()))
        }

        editTextSpecialNotifier.doAfterTextChanged {
            actions.onNext(AddSpecialEventAction.NotifierUpdated(it.toString()))
        }

        editTextSpecialDetails.doAfterTextChanged {
            actions.onNext(AddSpecialEventAction.NotesUpdated(it.toString()))
        }

        specialRadioGroup.setOnCheckedChangeListener { _, selectedId ->
            val kind = if (selectedId == R.id.button_damage) SpecialEventKind.damage else SpecialEventKind.event
            actions.onNext(AddSpecialEventAction.KindUpdated(kind))
        }

        saveSpecialEventButton.setOnClickListener {
            actions.onNext(AddSpecialEventAction.SaveSpecialEvent)
        }
    }

    private fun onTitleErrorChanged(error: Int?) {
        error?.let {
            textInputLayoutSpecialTitle.isErrorEnabled = true
            textInputLayoutSpecialTitle.error = getString(it)
        } ?: kotlin.run {
            textInputLayoutSpecialTitle.isErrorEnabled = false
            textInputLayoutSpecialTitle.isHintEnabled = true
        }
    }

    private fun onNotifierErrorChanged(error: Int?) {
        error?.let {
            textInputLayoutSpecialNotifier.isErrorEnabled = true
            textInputLayoutSpecialNotifier.error = getString(it)
        } ?: kotlin.run {
            textInputLayoutSpecialNotifier.isErrorEnabled = false
            textInputLayoutSpecialNotifier.isHintEnabled = true
        }
    }

    private fun onKindErrorChanged(error: Int?) {
        val radioButton = specialRadioGroup.getChildAt(specialRadioGroup.childCount - 1) as RadioButton
        radioButton.error = error?.let { getString(error) }
    }

    private fun onPopView() {
        // todo: move this to a navigator?
        requireActivity().supportFragmentManager.popBackStack(
            StackName.ShowAddSpecialEventsView.name,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun onHideKeyboard() {
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editTextTextSpecialTitle.rootView.windowToken, 0)
    }

    private fun onIncompleteError() {
        val text = getText(R.string.special_event_error_incomplete)
        Snackbar.make(specialRadioGroup, text, 5000).show()
    }
}
