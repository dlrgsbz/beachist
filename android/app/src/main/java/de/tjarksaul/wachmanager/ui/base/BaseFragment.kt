package de.tjarksaul.wachmanager.ui.base

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    protected fun isNetworkConnected(): Boolean {
        val connectivityManager =
            context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    protected fun getStationId(): String {
        return context?.getSharedPreferences("WachmanagerSettings", Context.MODE_PRIVATE)?.getString("stationId", "170b0488-6641-4794-ab32-f46eb370fc13") ?: "170b0488-6641-4794-ab32-f46eb370fc13"
    }

    protected fun showInternetConnectionError() {
        AlertDialog.Builder(context).setTitle("Keine Internetverbindung")
            .setMessage("Bitte die Internetverbindung prüfen und erneut probieren.")
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .setIcon(android.R.drawable.ic_dialog_alert).show()
    }
}