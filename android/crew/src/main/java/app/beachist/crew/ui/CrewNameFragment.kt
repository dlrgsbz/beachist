package app.beachist.crew.ui

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import app.beachist.crew.R
import app.beachist.crew.databinding.FragmentCrewNameBinding
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import org.koin.androidx.viewmodel.ext.android.viewModel


class CrewNameFragment : Fragment() {
    private val disposable = CompositeDisposable()

    private val actions: PublishSubject<CrewNameViewAction> = PublishSubject.create()

    private val _dismissPublisher: PublishSubject<DismissAction> = PublishSubject.create()
    val dismissPublisher: Observable<DismissAction> = _dismissPublisher

    private val viewModel: CrewNameViewModel by viewModel()

    private var _binding: FragmentCrewNameBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCrewNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()

        setupBindings()
    }

    override fun onResume() {
        super.onResume()

        actions.onNext(CrewNameViewAction.UpdateDate)
    }

    private fun setupView() {
        binding.editCrewName.requestFocus()
    }

    private fun hideKeyboard() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = requireActivity().currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        disposable += viewModel.stateOf { stationName }
            .subscribe {
                val stationString = resources.getString(R.string.station_name, it)
                binding.textStationName.text = stationString
            }

        disposable += viewModel.stateOf { currentDate }
            .subscribe {
                binding.dateTextView.text = it
            }

        binding.editCrewName.addTextChangedListener(object : TextChangeListener() {
            override fun onTextChanged(s: Editable?) {
                s?.toString()?.let {
                    actions.onNext(CrewNameViewAction.UpdateCrewName(it))
                }
            }
        })

        binding.startButton.setOnClickListener {
            actions.onNext(CrewNameViewAction.Submit)
        }

        disposable += viewModel.effect<CrewNameViewEffect.Dismiss>()
            .subscribe { dismiss() }
    }

    private fun dismiss() {
        hideKeyboard()

        _dismissPublisher.onNext(DismissAction.Dismiss)
    }
}