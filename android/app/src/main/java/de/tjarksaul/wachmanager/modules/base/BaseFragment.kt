package de.tjarksaul.wachmanager.modules.base

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import de.tjarksaul.wachmanager.dtos.Station

open class BaseFragment : Fragment() {
    protected fun isNetworkConnected(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @Deprecated("Please use StationRepository", replaceWith = ReplaceWith(
        expression = "StationRepository().getStoredStationId()",
        imports = ["de.tjarksaul.wachmanager.repositories.StationRepository"])
    )
    protected fun getStoredStationId(): String? {
        return requireContext().getSharedPreferences("WachmanagerSettings", Context.MODE_PRIVATE)
            .getString("stationId", null)
    }

    @Deprecated("Please use StationRepository", replaceWith = ReplaceWith(
        expression = "StationRepository().getStationId()",
        imports = ["de.tjarksaul.wachmanager.repositories.StationRepository"])
    )
    protected fun getStationId(): String {
        return getStoredStationId()
            ?: throw IllegalStateException("No stationId found.")
    }

    protected fun getCrew(): String {
        return requireContext().getSharedPreferences("WachmanagerSettings", Context.MODE_PRIVATE)
            .getString("crewNames", "") ?: ""
    }

    protected fun saveVal(name: String, value: String) {
        val editor =
            requireContext().getSharedPreferences("WachmanagerSettings", Context.MODE_PRIVATE)
                .edit()
        editor.putString(name, value)
        editor.apply()
    }

    protected fun saveIntVal(name: String, value: Long) {
        val editor =
            requireContext().getSharedPreferences("WachmanagerSettings", Context.MODE_PRIVATE)
                .edit()
        editor.putLong(name, value)
        editor.apply()
    }

    protected fun showInternetConnectionError() {
        AlertDialog.Builder(context).setTitle("Keine Internetverbindung")
            .setMessage("Bitte die Internetverbindung prüfen und erneut probieren.")
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .setIcon(android.R.drawable.ic_dialog_alert).show()
    }

    protected fun cacheStations(stations: List<Station>) {
        val gson = Gson()
        val stationString = gson.toJson(stations)

        saveVal("cachedStations", stationString)
    }

    protected fun cachedStations(): List<Station>? {
        val value = requireContext().getSharedPreferences("WachmanagerSettings", Context.MODE_PRIVATE)
            .getString("cachedStations", null)
            ?: return null

        val gson = Gson()
        return try {
            gson.fromJson(value, Array<Station>::class.java).toList()
        } catch (ex: JsonSyntaxException) {
            null
        }
    }
}