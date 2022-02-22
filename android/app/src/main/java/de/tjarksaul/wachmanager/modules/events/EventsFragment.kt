package de.tjarksaul.wachmanager.modules.events

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import de.tjarksaul.wachmanager.GlobalAction
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.modules.base.BaseFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_events.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class EventsFragment : BaseFragment() {
    private val disposable = CompositeDisposable()

    private val viewModel: EventViewModel by viewModel()

    private val actions: PublishSubject<EventListAction> = PublishSubject.create()
    private val adapter: EventsListAdapter by lazy { EventsListAdapter(actions) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!isNetworkConnected()) {
            AlertDialog.Builder(context).setTitle("Keine Internetverbindung")
                .setMessage("Please check your internet connection and try again")
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
        }


        return inflater.inflate(R.layout.fragment_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupBindings()

        actions.onNext(EventListAction.Refetch)
    }

    private fun setupView() {
        val layoutManager = LinearLayoutManager(activity)
        eventList.layoutManager = layoutManager
        eventList.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(
            eventList.context,
            layoutManager.orientation
        )
        eventList.addItemDecoration(dividerItemDecoration)
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        firstAidButton.setOnClickListener {
            actions.onNext(EventListAction.AddEventClicked)
        }

        undoButton.setOnClickListener {
            actions.onNext(EventListAction.CancelClicked)
        }

        disposable += viewModel.stateOf { canAdd }
            .subscribe { canAdd ->
                if (undoButton !== null) {
                    undoButton.visibility = if (canAdd) View.INVISIBLE else View.VISIBLE
                }
                if (firstAidButton !== null) {
                    firstAidButton.isEnabled = canAdd
                }
            }

        disposable += viewModel.stateOf { eventItems }
            .subscribe { adapter.items = it }
    }
}