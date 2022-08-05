package app.beachist.crew.ui

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.beachist.crew.R
import app.beachist.crew.databinding.FragmentCrewNameBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

@ExperimentalCoroutinesApi
class CrewNameFragment : DialogFragment() {
    private val actions: MutableStateFlow<CrewNameViewAction?> = MutableStateFlow(null)

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

        binding.editCrewName.requestFocus()

        setupBindings()
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
    }

    override fun onResume() {
        super.onResume()

        actions.tryEmit(CrewNameViewAction.UpdateDate)
    }

    private fun hideKeyboard() {
        val activity = this.activity ?: return
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateOf { stationName }
                    .onEach {
                        val stationString = resources.getString(R.string.station_name, it)
                        binding.textStationName.text = stationString
                    }
                    .launchIn(this)

                viewModel.stateOf { currentDate }
                    .onEach {
                        binding.dateTextView.text = it
                    }
                    .launchIn(this)

                binding.editCrewName.addTextChangedListener(object : TextChangeListener() {
                    override fun onTextChanged(s: Editable?) {
                        s?.toString()?.let {
                            actions.tryEmit(CrewNameViewAction.UpdateCrewName(it))
                        }
                    }
                })

                binding.startButton.setOnClickListener {
                    actions.tryEmit(CrewNameViewAction.Submit)
                }

                viewModel.effect<CrewNameViewEffect.Dismiss>()
                    .onEach { dismiss() }
                    .launchIn(this)
            }
        }
    }

    override fun dismiss() {
        Timber.tag("CrewNameFragment").d("dismiss")
        hideKeyboard()
        super.dismiss()
    }
}