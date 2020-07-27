package de.tjarksaul.wachmanager

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.tjarksaul.wachmanager.ui.splash.SplashFragment

class MainActivity : AppCompatActivity() {

    private fun stationHasBeenSelectedToday(): Boolean {
        val context = applicationContext
        val date = context.getSharedPreferences("WachmanagerSettings", Context.MODE_PRIVATE)
            ?.getLong("lastStationSelectedDate", 0) ?: 0

        return DateUtils.isToday(date)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (stationHasBeenSelectedToday()) {
            showStationView()
        } else {
            val fragmentManager: FragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.add(android.R.id.content, SplashFragment())
            transaction.commit()
        }
    }

    fun showStationView() {
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_settings,
                R.id.navigation_special_events
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            finish();
        }
    }
}