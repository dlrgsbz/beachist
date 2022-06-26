package de.tjarksaul.wachmanager.modules.specialEvents.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.modules.base.BaseFragment
import de.tjarksaul.wachmanager.modules.specialEvents.add.AddSpecialEventFragment
import de.tjarksaul.wachmanager.util.StackName
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_special_events.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ext.android.viewModel


@ExperimentalCoroutinesApi
class SpecialEventsFragment : BaseFragment() {
    private val disposables = CompositeDisposable()

    private val viewModel: SpecialEventsViewModel by viewModel()

    private val actions: PublishSubject<SpecialEventListAction> = PublishSubject.create()
    private val adapter: SpecialEventsListAdapter by lazy {
        SpecialEventsListAdapter(getKoin().get())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_special_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupBindings()
    }

    private fun setupView() {
        specialEventList.adapter = adapter
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        disposables += viewModel.stateOf { eventItems }
            .subscribe { adapter.items = it }

        add_special_event_fab.setOnClickListener {
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

        transaction.commit()
    }
}