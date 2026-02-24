package com.dashkin.busradar

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.dashkin.busradar.databinding.ActivityMainBinding
import com.dashkin.busradar.feature.busdetail.presentation.fragment.BusDetailFragment
import com.dashkin.busradar.feature.map.presentation.fragment.MapFragment
import com.dashkin.busradar.feature.search.presentation.fragment.SearchFragment
import com.dashkin.busradar.feature.settings.presentation.fragment.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (application as BusRadarApp).appComponent.inject(this)

        setupBackHandler()
        setupBottomNavigation()

        if (savedInstanceState == null) showTab(MapFragment.TAG) { MapFragment() }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> { showTab(MapFragment.TAG) { MapFragment() }; true }
                R.id.nav_search -> { showTab(SearchFragment.TAG) { SearchFragment() }; true }
                R.id.nav_settings -> { showTab(SettingsFragment.TAG) { SettingsFragment() }; true }
                else -> false
            }
        }
    }

    // Shows a bottom-nav tab fragment using show/hide so each tab preserves its state.
    // A new instance is created only on the first visit.
    private fun showTab(tag: String, creator: () -> Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.fragments.forEach { transaction.hide(it) }

        val existing = supportFragmentManager.findFragmentByTag(tag)
        if (existing == null) transaction.add(R.id.fragment_container, creator(), tag)
        else transaction.show(existing)
        transaction.commit()
    }

    // Opens BusDetail as a full-screen overlay, hiding the bottom navigation.
    fun openBusDetail(vehicleId: String) {
        binding.bottomNavigation.isVisible = false
        supportFragmentManager.beginTransaction()
            .add(
                R.id.fragment_container,
                BusDetailFragment.newInstance(vehicleId),
                BusDetailFragment.TAG,
            )
            .addToBackStack(BusDetailFragment.TAG)
            .commit()
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                        binding.bottomNavigation.isVisible = true
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )
    }
}
