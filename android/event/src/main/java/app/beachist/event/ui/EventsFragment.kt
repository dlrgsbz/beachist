package app.beachist.event.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.beachist.event.databinding.FragmentEventsBinding
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import org.koin.androidx.viewmodel.ext.android.viewModel


class EventsFragment : Fragment() {
    private val disposable = CompositeDisposable()

    private val viewModel: EventViewModel by viewModel()

    private val actions: PublishSubject<EventListAction> = PublishSubject.create()
    private val adapter: EventsListAdapter by lazy { EventsListAdapter(actions) }

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

        actions.onNext(EventListAction.Refetch)
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
            actions.onNext(EventListAction.AddEventClicked)
        }

        binding.undoButton.setOnClickListener {
            actions.onNext(EventListAction.CancelClicked)
        }

        disposable += viewModel.stateOf { canAdd }
            .subscribe { canAdd ->
                binding.undoButton.visibility = if (canAdd) View.INVISIBLE else View.VISIBLE
                binding.firstAidButton.isEnabled = canAdd
            }

        disposable += viewModel.stateOf { eventItems }
            .subscribe { adapter.items = it }
    }
}