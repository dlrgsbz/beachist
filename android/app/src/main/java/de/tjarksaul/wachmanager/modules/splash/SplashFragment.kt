package de.tjarksaul.wachmanager.modules.splash

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.modules.main.MainActivity
import de.tjarksaul.wachmanager.util.TextChangeListener
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_splash.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class SplashFragment : Fragment() {
    private val disposable = CompositeDisposable()

    private val actions: PublishSubject<SplashViewAction> = PublishSubject.create()

    private val splashViewModel: SplashViewModel by viewModel()

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()

        setupBindings()
    }

    override fun onResume() {
        super.onResume()

        actions.onNext(SplashViewAction.Refetch)
    }

    private fun setupView() {
        editCrewName.requestFocus()
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
        splashViewModel.attach(actions)

        disposable += splashViewModel.stateOf { stationId }
            .subscribe {
                val stationString = resources.getString(R.string.station_name, it)
                textStationName.text = stationString
            }

        disposable += splashViewModel.stateOf { currentDate }
            .subscribe {
                dateTextView.text = it
            }

        editCrewName.addTextChangedListener(object : TextChangeListener() {
            override fun onTextChanged(s: Editable?) {
                s?.toString()?.let {
                    actions.onNext(SplashViewAction.UpdateCrewName(it))
                }
            }
        })

        startButton.setOnClickListener {
            actions.onNext(SplashViewAction.Submit)
        }

        disposable += splashViewModel.effect<SplashViewEffect.Dismiss>()
            .subscribe { dismiss() }
    }

    private fun dismiss() {
        hideKeyboard()

        val activity: MainActivity = activity as MainActivity

        activity.goToStationView()
    }
}