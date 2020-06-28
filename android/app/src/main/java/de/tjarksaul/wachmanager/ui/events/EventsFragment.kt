package de.tjarksaul.wachmanager.ui.events

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.api.HTTPRepo
import de.tjarksaul.wachmanager.dtos.EventStats
import de.tjarksaul.wachmanager.dtos.EventType
import de.tjarksaul.wachmanager.dtos.IdResponse
import de.tjarksaul.wachmanager.ui.base.BaseFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventsFragment : BaseFragment() {
    private val httpRepo = HTTPRepo()

    private lateinit var eventViewModel: EventViewModel
    private lateinit var undoButton: Button
    private lateinit var firstAidButton: Button

    private var canceled = false

    private val eventsCallback = object : Callback<EventStats> {
        override fun onFailure(call: Call<EventStats>?, t: Throwable?) {
            Log.e("MainActivity", "Problem calling Github API {${t?.message}}")
        }

        override fun onResponse(call: Call<EventStats>?, response: Response<EventStats>?) {
            response?.isSuccessful.let {
                val data = response?.body()
                if (data != null) {
                    eventViewModel.updateData(data)
                }
            }
        }
    }

    private val incrementCallback = object : Callback<IdResponse> {
        override fun onFailure(call: Call<IdResponse>?, t: Throwable?) {
            Log.e("MainActivity", "Problem calling Github API {${t?.message}}")
        }

        override fun onResponse(call: Call<IdResponse>?, response: Response<IdResponse>?) {
            response?.isSuccessful.let {
                val data = response?.body()
                if (data != null) {
                    httpRepo.getEvents(getStationId(), eventsCallback)
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


        eventViewModel =
                ViewModelProviders.of(this).get(EventViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_events, container, false)
        val textView: TextView = root.findViewById(R.id.firstAidCount)
        eventViewModel.firstAid.observe(viewLifecycleOwner, Observer {
            textView.text = it.toString()
        })

        undoButton = root.findViewById(R.id.undoButton)
        undoButton.visibility = View.INVISIBLE
        undoButton.setOnClickListener {
            canceled = true
            firstAidButton.isEnabled = true
            undoButton.visibility = View.INVISIBLE
            eventViewModel.decrement()
        }

        firstAidButton = root.findViewById(R.id.firstAidButton)
        firstAidButton.setOnClickListener {
            firstAidButton.isEnabled = false
            undoButton.visibility = View.VISIBLE
            canceled = false
            eventViewModel.increment()

            Handler().postDelayed({
                increment()
            }, 10000)

        }

        return root
    }

    private fun increment() {
        if (canceled) {
            return
        }

        httpRepo.createEvent(getStationId(), EventType.firstAid, incrementCallback)

        firstAidButton.isEnabled = true
        undoButton.visibility = View.INVISIBLE

    }
}