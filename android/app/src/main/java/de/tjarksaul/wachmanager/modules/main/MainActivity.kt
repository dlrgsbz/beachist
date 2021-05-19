package de.tjarksaul.wachmanager.modules.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.tjarksaul.wachmanager.R
import de.tjarksaul.wachmanager.modules.splash.SplashFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()
    private var splashFragment: SplashFragment? = null
    private val disposable = CompositeDisposable()
    private val actions: PublishSubject<MainViewAction> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
    }

    override fun onResume() {
        super.onResume()

        setupBindings()
    }

    private fun setupView() {
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)

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

    private fun setupBindings() {
        viewModel.attach(actions)

        disposable += viewModel.stateOf { shouldShowStationSelection }
            .subscribe { setupSplashView(it) }
    }

    private fun setupSplashView(shouldShow: Boolean) {
        if (!shouldShow) {
            return
        }

        splashFragment = SplashFragment()

        supportFragmentManager.beginTransaction()
            .add(android.R.id.content, splashFragment!!)
            .commit()
    }

    fun goToStationView() {
        if (splashFragment == null) {
            throw IllegalStateException("splashFragment is null")
        }

        supportFragmentManager.beginTransaction()
            .remove(splashFragment!!)
            .commit()

        splashFragment = null
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            finish()
        }
    }
}
