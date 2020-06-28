package de.tjarksaul.wachmanager.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.api.HTTPRepo
import de.tjarksaul.wachmanager.config.configPassword
import de.tjarksaul.wachmanager.dtos.Station
import de.tjarksaul.wachmanager.ui.base.BaseFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SettingsFragment : BaseFragment() {
    private val httpRepo = HTTPRepo()

    private lateinit var settingsViewModel: SettingsViewModel
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
                settingsViewModel.updateData(data)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
            ViewModelProviders.of(this).get(SettingsViewModel::class.java)

        if (isNetworkConnected()) {
            httpRepo.getStations(callback)
        } else {
            showInternetConnectionError()
        }

        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        listView = root.findViewById(R.id.settingsList)
        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

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
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity!!)
        builder.setTitle("Bitte Passwort eingeben")

        val input = EditText(activity!!)
        input.inputType = InputType.TYPE_NUMBER_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton(
            "OK"
        ) { _, _ ->
            if (input.text.toString() == configPassword) {
                println("correct password")
                val updateSelectedStation = updateSelectedStation(value)
                if (updateSelectedStation) {
                    listView.setItemChecked(position, true)
                }
            } else {
                println("wrong password")
            }
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }

        builder.show()
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