package app.beachist.station_check.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import app.beachist.station_check.R
import app.beachist.station_check.databinding.FragmentStationCheckBinding
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class StationCheckFragment : Fragment() {
    private val disposable = CompositeDisposable()

    private val viewModel: StationCheckViewModel by viewModel()

    private val actions: PublishSubject<StationCheckAction> = PublishSubject.create()
    private val adapter: StationCheckListAdapter by lazy {
        StationCheckListAdapter(
            actions
        )
    }

    private var _binding: FragmentStationCheckBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.saveState()

        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStationCheckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupBindings()

        actions.onNext(StationCheckAction.Refresh)
    }

    private fun setupView() {
        binding.stationCheckList.adapter = adapter
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        disposable += viewModel.stateOf { entries }
            .doOnNext { Timber.tag("StationCheckFragment").d("%s", it) }
            .subscribe {
                adapter.items = it
            }

        disposable += viewModel.effects()
            .ofType<StationCheckEffect.ShowNoteBox>()
            .subscribe { showNoteInput(it.id) }

        disposable += viewModel.effects()
            .ofType<StationCheckEffect.ShowAmountBox>()
            .subscribe { showAmountInput(it.id) }
    }

    private fun showNoteInput(itemId: String) {
        val activity = requireActivity()
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.station_check_note_description))

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton(
            getString(R.string.station_check_note_button_okay)
        ) { _, _ ->
            val note = input.text.toString()
            actions.onNext(StationCheckAction.AddItemNote(itemId, note))
        }

        builder.setNegativeButton(
            getString(R.string.station_check_note_button_cancel)
        ) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showAmountInput(itemId: String) {
        val activity = requireActivity()
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle(getText(R.string.station_check_amount_description))

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText("0")
        builder.setView(input)

        builder.setPositiveButton(
            getString(R.string.station_check_note_button_okay)
        ) { _, _ ->
            val amount = try {
                input.text.toString().toInt()
            } catch (e: NumberFormatException) {
                0
            }

            actions.onNext(StationCheckAction.AddItemAmount(itemId, amount))
        }
        builder.setNegativeButton(
            getString(R.string.station_check_note_button_cancel)
        ) { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}