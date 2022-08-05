package app.beachist.main

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import app.beachist.crew.ui.CrewNameFragment
import app.beachist.provision.ui.ProvisionFragment
import app.beachist.service.BeachistService
import app.beachist.R
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalCoroutinesApi
class MainActivity: AppCompatActivity(), ServiceConnection {

    private val viewModel: MainViewModel by viewModel()
    private var crewNameFragment: CrewNameFragment? = null
    private var provisionFragment: ProvisionFragment? = null
    private val disposable = CompositeDisposable()
    private val actions: PublishSubject<MainViewAction> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindService(Intent(this, BeachistService::class.java), this, Service.BIND_AUTO_CREATE)

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
                R.id.navigation_special_events
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun setupBindings() {
        viewModel.attach(actions)

        disposable += viewModel.stateOf { currentView }
            .subscribe { onViewChange(it) }
    }

    private fun onViewChange(view: MainViewCurrentView) {
        when (view) {
            MainViewCurrentView.CrewInput -> setupCrewView()
            MainViewCurrentView.Provision -> setupProvisioningView()
            MainViewCurrentView.TabbedView -> goToStationView()
        }
    }

    private fun setupCrewView() {
        val fragment = CrewNameFragment()

        disposable += fragment.dismissPublisher.subscribe {
            goToStationView()
        }

        supportFragmentManager.beginTransaction()
            .add(android.R.id.content, fragment)
            .commit()

        crewNameFragment = fragment
    }

    private fun setupProvisioningView() {
        val fragment = ProvisionFragment()

        supportFragmentManager.beginTransaction()
            .add(android.R.id.content, fragment)
            .commit()

        provisionFragment = fragment
    }

    private fun goToStationView() {
        val fragment = when {
            provisionFragment != null -> provisionFragment
            crewNameFragment != null -> crewNameFragment
            else -> null
        } ?: return

        supportFragmentManager.beginTransaction()
            .remove(fragment)
            .commit()

        crewNameFragment = null
        provisionFragment = null
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            finish()
        }
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        // ignoring this on purpose
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        // ignoring this on purpose
    }
}
