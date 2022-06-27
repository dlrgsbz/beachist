package de.tjarksaul.wachmanager.modules.stationCheck

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import de.tjarksaul.wachmanager.R
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_station_check.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
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

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.saveState()

        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_station_check, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupBindings()

        actions.onNext(StationCheckAction.Refresh)
    }

    private fun setupView() {
        stationCheckList.adapter = adapter
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        disposable += viewModel.stateOf { entries }
            .doOnNext { Timber.tag("StationCheckFragment").d("%s", it) }
            .subscribe {
                adapter.items = it
                adapter.notifyDataSetChanged()
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
        builder.setTitle("Bitte das Problem genauer beschreiben")

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton(
            "OK"
        ) { _, _ ->
            val note = input.text.toString()
            actions.onNext(StationCheckAction.AddItemNote(itemId, note))
        }

        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun showAmountInput(itemId: String) {
        val activity = requireActivity()
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle("Wie viel ist noch vorhanden?")

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText("0")
        builder.setView(input)

        builder.setPositiveButton(
            "OK"
        ) { _, _ ->
            val amount = try {
                input.text.toString().toInt()
            } catch (e: NumberFormatException) {
                0
            }

            actions.onNext(StationCheckAction.AddItemAmount(itemId, amount))
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}