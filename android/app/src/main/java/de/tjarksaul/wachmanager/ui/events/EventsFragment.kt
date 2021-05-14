package de.tjarksaul.wachmanager.ui.events

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import de.tjarksaul.wachmanager.GlobalAction
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.api.HTTPRepo
import de.tjarksaul.wachmanager.dtos.EventStats
import de.tjarksaul.wachmanager.ui.base.BaseFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_events.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EventsFragment : BaseFragment() {
    private val httpRepo = HTTPRepo()
    private val disposable = CompositeDisposable()

    private val viewModel: EventViewModel by viewModel()

    private val actions: PublishSubject<EventListAction> = PublishSubject.create()
    private val globalActions: PublishSubject<GlobalAction> = PublishSubject.create()
    private val adapter: EventsListAdapter by lazy { EventsListAdapter(actions) }

    private val eventsCallback = object : Callback<EventStats> {
        override fun onFailure(call: Call<EventStats>?, t: Throwable?) {
            Log.e("MainActivity", "Problem calling Github API {${t?.message}}")
        }

        override fun onResponse(call: Call<EventStats>?, response: Response<EventStats>?) {
            response?.isSuccessful.let {
                val data = response?.body()
                if (data != null) {
                    viewModel.updateData(data)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isNetworkConnected()) {
            httpRepo.getEvents(getStationId(), eventsCallback)
        } else {
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

        globalActions.onNext(GlobalAction.Refetch)
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
        viewModel.globalStore.attach(globalActions)

        firstAidButton.setOnClickListener {
            globalActions.onNext(GlobalAction.AddEventClicked)
        }

        undoButton.setOnClickListener {
            globalActions.onNext(GlobalAction.CancelClicked)
        }

        disposable += viewModel.globalStore.stateOf { canAdd }
            .subscribe { canAdd ->
                if (undoButton !== null) {
                    undoButton.visibility = if (canAdd) View.INVISIBLE else View.VISIBLE
                }
                if (firstAidButton !== null) {
                    firstAidButton.isEnabled = canAdd
                }
            }

        disposable += viewModel.globalStore.stateOf { eventItems }
            .subscribe { adapter.items = it }
    }
}