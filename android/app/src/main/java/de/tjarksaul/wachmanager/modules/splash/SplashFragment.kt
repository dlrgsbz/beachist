package de.tjarksaul.wachmanager.modules.splash

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import de.tjarksaul.wachmanager.modules.main.MainActivity
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.api.HTTPRepo
import de.tjarksaul.wachmanager.dtos.Station
import de.tjarksaul.wachmanager.modules.base.BaseFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class SplashFragment : BaseFragment() {
    private val httpRepo = HTTPRepo()

    private lateinit var listView: ListView

    private val callback = object : Callback<MutableList<Station>> {
        override fun onFailure(call: Call<MutableList<Station>>?, t: Throwable?) {
            Log.e("MainActivity", "Problem calling Wachmanager API {${t?.message}}")
            showInternetConnectionError()
        }

        override fun onResponse(
            call: Call<MutableList<Station>>?,
            response: Response<MutableList<Station>>?
        ) {
            response?.isSuccessful.let {
                val data = response?.body() ?: emptyList<Station>().toMutableList()

                if (data.count() > 0) {
                    cacheStations(data)
                }

                updateModel(data)
            }
        }
    }

    private fun updateModel(data: List<Station>) {
        splashViewModel.updateData(data.toMutableList())

        if (getStoredStationId() != null) {
            val index =
                splashViewModel.stations.value?.indexOfFirst { it.id == getStoredStationId() }

            index?.let { stationNameView.setSelection(index) }
        }
    }

    val splashViewModel: SplashViewModel by viewModels()
    private lateinit var stationNameView: Spinner
    private lateinit var crewNameField: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_splash, container, false)
        stationNameView = root.findViewById(R.id.stationName)
        val dateTextView: TextView = root.findViewById(R.id.dateTextView)
        crewNameField = root.findViewById(R.id.editTextTextPersonName)

        val cachedStations = cachedStations()
        if (cachedStations != null) {
            updateModel(cachedStations)
        }

        if (isNetworkConnected()) {
            httpRepo.getStations(callback)
        } else {
            showInternetConnectionError()
        }


        val format = SimpleDateFormat("dd.MM.yyyy")
        dateTextView.text = format.format(Date())

        splashViewModel.stations.observe(viewLifecycleOwner, Observer {
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                it
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                stationNameView.adapter = adapter
            }
        })

        val startButton: Button = root.findViewById(R.id.startButton)

        startButton.setOnClickListener {
            setStationAndCrewNames()

            val activity: MainActivity = activity as MainActivity

            activity.goToStationView()
        }

        return root
    }

    private fun setStationAndCrewNames() {
        val crewNames = crewNameField.text.toString()
        val station = stationNameView.selectedItem as Station
        val stationName = station.name
        val stationId = station.id

        saveVal("stationId", stationId)
        saveVal("stationName", stationName)
        saveVal("crewNames", crewNames)

        saveIntVal("lastStationSelectedDate", Date().time)
    }

}