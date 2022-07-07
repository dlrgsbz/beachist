package app.beachist.special_event.add

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import app.beachist.special_event.R
import app.beachist.special_event.databinding.FragmentAddSpecialEventBinding
import app.beachist.special_event.dtos.SpecialEventKind
import app.beachist.special_event.util.StackName
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import org.koin.androidx.viewmodel.ext.android.viewModel


internal class AddSpecialEventFragment : Fragment() {
    private val disposables = CompositeDisposable()

    private val viewModel: AddSpecialEventViewModel by viewModel()

    private val actions: PublishSubject<AddSpecialEventAction> = PublishSubject.create()

    private var _binding: FragmentAddSpecialEventBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddSpecialEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.dispose()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupBindings()
    }

    private fun setupView() {
        binding.textInputLayoutSpecialTitle.isHintEnabled = true
        binding.textInputLayoutSpecialTitle.hint = getString(R.string.special_event_title_hint)

        binding.textInputLayoutSpecialNotifier.isHintEnabled = true
        binding.textInputLayoutSpecialNotifier.hint = getString(R.string.special_event_notifier_hint)
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        disposables += viewModel.stateOf { titleError }
            .subscribe { onTitleErrorChanged(it) }

        disposables += viewModel.stateOf { notifierError }
            .subscribe { onNotifierErrorChanged(it) }

        disposables += viewModel.stateOf { kindError }
            .subscribe { onKindErrorChanged(it) }

        disposables += viewModel.stateOf { noteError }
            .subscribe { onNoteErrorChanged(it) }

        disposables += viewModel.stateOf { saveButtonEnabled }
            .subscribe { binding.saveSpecialEventButton.isEnabled = it }

        disposables += viewModel.effects()
            .ofType<AddSpecialEventEffect.PopView>()
            .subscribe { onPopView() }

        disposables += viewModel.effects()
            .ofType<AddSpecialEventEffect.IncompleteError>()
            .subscribe { onIncompleteError() }

        disposables += viewModel.effects()
            .ofType<AddSpecialEventEffect.HideKeyboard>()
            .subscribe { onHideKeyboard() }

        binding.editTextTextSpecialTitle.doAfterTextChanged {
            actions.onNext(AddSpecialEventAction.TitleUpdated(it.toString()))
        }

        binding.editTextSpecialNotifier.doAfterTextChanged {
            actions.onNext(AddSpecialEventAction.NotifierUpdated(it.toString()))
        }

        binding.editTextSpecialDetails.doAfterTextChanged {
            actions.onNext(AddSpecialEventAction.NotesUpdated(it.toString()))
        }

        binding.specialRadioGroup.setOnCheckedChangeListener { _, selectedId ->
            val kind = if (selectedId == R.id.button_damage) SpecialEventKind.damage else SpecialEventKind.event
            actions.onNext(AddSpecialEventAction.KindUpdated(kind))
        }

        binding.saveSpecialEventButton.setOnClickListener {
            actions.onNext(AddSpecialEventAction.SaveSpecialEvent)
        }
    }

    private fun onTitleErrorChanged(error: ErrorState) {
        when (error) {
            ErrorState.NoError -> {
                binding.textInputLayoutSpecialTitle.isErrorEnabled = false
                binding.textInputLayoutSpecialTitle.isHintEnabled = true
            }
            is ErrorState.HasError -> {
                binding.textInputLayoutSpecialTitle.isErrorEnabled = true
                binding.textInputLayoutSpecialTitle.error = getString(error.error)
            }
        }
    }

    private fun onNotifierErrorChanged(error: ErrorState) {
        when (error) {
            ErrorState.NoError -> {
                binding.textInputLayoutSpecialNotifier.isErrorEnabled = false
                binding.textInputLayoutSpecialNotifier.isHintEnabled = true
            }
            is ErrorState.HasError -> {
                binding.textInputLayoutSpecialNotifier.isErrorEnabled = true
                binding.textInputLayoutSpecialNotifier.error = getString(error.error)
            }
        }
    }

    private fun onNoteErrorChanged(error: ErrorState) {
        when (error) {
            ErrorState.NoError -> {
                binding.textInputLayoutSpecialNote.isErrorEnabled = false
                binding.textInputLayoutSpecialNote.isHintEnabled = true
            }
            is ErrorState.HasError -> {
                binding.textInputLayoutSpecialNote.isErrorEnabled = true
                binding.textInputLayoutSpecialNote.error = getString(error.error)
            }
        }
    }

    private fun onKindErrorChanged(error: ErrorState) {
        val radioButton = binding.specialRadioGroup.getChildAt(binding.specialRadioGroup.childCount - 1) as RadioButton
        radioButton.error = when (error) {
            ErrorState.NoError -> null
            is ErrorState.HasError ->getString(error.error)
        }
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
        imm.hideSoftInputFromWindow(binding.editTextTextSpecialTitle.rootView.windowToken, 0)
    }

    private fun onIncompleteError() {
        val text = getText(R.string.special_event_error_incomplete)
        Snackbar.make(binding.specialRadioGroup, text, 5000).show()
    }
}
