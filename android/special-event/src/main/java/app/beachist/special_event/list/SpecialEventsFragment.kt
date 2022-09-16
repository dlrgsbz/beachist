package app.beachist.special_event.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.beachist.special_event.add.AddSpecialEventFragment
import app.beachist.special_event.databinding.FragmentSpecialEventsBinding
import app.beachist.special_event.util.StackName
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.viewModel


@ExperimentalCoroutinesApi
class SpecialEventsFragment : Fragment() {
    private val disposables = CompositeDisposable()

    private val viewModel: SpecialEventsViewModel by viewModel()

    private val actions: PublishSubject<SpecialEventListAction> = PublishSubject.create()
    private val adapter: SpecialEventsListAdapter by lazy {
        SpecialEventsListAdapter(getKoin().get())
    }

    private var _binding: FragmentSpecialEventsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSpecialEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupBindings()
    }

    private fun setupView() {
        binding.specialEventList.adapter = adapter
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        disposables += viewModel.stateOf { eventItems }
            .subscribe { adapter.items = it }

        binding.addSpecialEventFab.setOnClickListener {
            actions.onNext(SpecialEventListAction.CreateEvent)
        }

        disposables += viewModel.effects()
            .ofType<SpecialEventListEffect.ShowCreateEventView>()
            .subscribe { onShowCreateEventView() }
    }

    private fun onShowCreateEventView() {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(
            android.R.id.content,
            AddSpecialEventFragment(),
        )

        transaction.addToBackStack(StackName.ShowAddSpecialEventsView.name)

        transaction.commitAllowingStateLoss()
    }
}