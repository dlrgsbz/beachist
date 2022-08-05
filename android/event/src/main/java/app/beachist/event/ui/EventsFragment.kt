package app.beachist.event.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.beachist.event.databinding.FragmentEventsBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


@ExperimentalCoroutinesApi
class EventsFragment : Fragment() {
    private val viewModel: EventViewModel by viewModel()

    private val actions: MutableStateFlow<EventListAction?> = MutableStateFlow(null)
    private val adapter: EventsListAdapter by lazy { EventsListAdapter() }

    private var _binding: FragmentEventsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupBindings()

        viewLifecycleOwner.lifecycleScope.launch {
            actions.emit(EventListAction.Refetch)
        }
    }

    private fun setupView() {
        val layoutManager = LinearLayoutManager(activity)
        binding.eventList.layoutManager = layoutManager
        binding.eventList.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(
            binding.eventList.context,
            layoutManager.orientation
        )
        binding.eventList.addItemDecoration(dividerItemDecoration)
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        binding.firstAidButton.setOnClickListener {
            actions.tryEmit(EventListAction.AddEventClicked)
        }

        binding.undoButton.setOnClickListener {
            actions.tryEmit(EventListAction.CancelClicked)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateOf { canAdd }
                    .onEach { canAdd ->
                        binding.undoButton.visibility = if (canAdd) View.INVISIBLE else View.VISIBLE
                        binding.firstAidButton.isEnabled = canAdd
                    }
                    .launchIn(this)

                viewModel.stateOf { eventItems }
                    .onEach { adapter.items = it }
                    .launchIn(this)
            }
        }
    }
}
