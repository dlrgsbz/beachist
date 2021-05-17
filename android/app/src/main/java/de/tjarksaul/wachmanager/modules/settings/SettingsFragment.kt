package de.tjarksaul.wachmanager.modules.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.api.HTTPRepo
import de.tjarksaul.wachmanager.dtos.Station
import de.tjarksaul.wachmanager.modules.base.BaseFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SettingsFragment : BaseFragment() {
    private val httpRepo = HTTPRepo()

    private val settingsViewModel: SettingsViewModel by viewModels()
    private lateinit var listView: ListView

    private val callback = object : Callback<MutableList<Station>> {
        override fun onFailure(call: Call<MutableList<Station>>?, t: Throwable?) {
            Log.e("MainActivity", "Problem calling Github API {${t?.message}}")
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
        settingsViewModel.updateData(data.toMutableList())

        if (getStoredStationId() != null) {
            val index =
                settingsViewModel.stations.value?.indexOfFirst { it.id == getStoredStationId() }

            index?.let { listView.setItemChecked(index, true) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        listView = root.findViewById(R.id.settingsList)
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

        val cachedStations = cachedStations()
        if (cachedStations != null) {
            updateModel(cachedStations)
        }

        if (isNetworkConnected()) {
            httpRepo.getStations(callback)
        } else {
            showInternetConnectionError()
        }

        listView.onItemClickListener =
            OnItemClickListener { adapter, v, position, arg3 ->
                val value = adapter.getItemAtPosition(position) as Station

                onSelect(value, position)
            }

        settingsViewModel.stations.observe(viewLifecycleOwner, Observer {
            val adapter = activity?.applicationContext?.let { it1 ->
                ArrayAdapter(it1, android.R.layout.simple_list_item_checked, it)
            }
            listView.adapter = adapter
        })
        return root
    }

    private fun onSelect(value: Station, position: Int) {
        val updateSelectedStation = updateSelectedStation(value)
        if (updateSelectedStation) {
            listView.setItemChecked(position, true)
        }
    }

    private fun updateSelectedStation(
        station: Station
    ): Boolean {
        val preferences = context?.getSharedPreferences("WachmanagerSettings", Context.MODE_PRIVATE)
        if (preferences != null) {
            val editor = preferences.edit()
            editor.putString("stationId", station.id)
            editor.apply()

            return true
        }
        return false
    }
}