package com.example.fraudmitigation

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.fraudmitigation.ui.dashboard.DashboardFragment
import com.example.fraudmitigation.ui.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_drawer_view)
        val bottomNavView: BottomNavigationView = findViewById(R.id.nav_view)

        // Set the navigation item selected listener
        navView.setNavigationItemSelectedListener(this)

        // Set the default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment_activity_main, HomeFragment()).commit()
        }

        // Handle bottom navigation item selections
        bottomNavView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_dashboard -> {
                    replaceFragment(DashboardFragment())
                    true
                }
                R.id.navigation_menu -> {
                    drawerLayout.openDrawer(GravityCompat.START)

                    true
                }
                else -> false
            }
        }

        // Enable the up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_notifications_black_24dp)
    }

    // Handle navigation item selections
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_fraud_mitigation -> {
                WarningDialogFragment().show(supportFragmentManager, "warning_dialog")
                // Handle Fraud Mitigation action
                // You can replace with a new fragment or perform an action
                true
            }
            R.id.nav_card_settings -> {
                // Handle Card Settings action
                // You can replace with a new fragment or perform an action
                true
            }
            else -> false
        }.also {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        return true
    }

    // Replace the current fragment with the specified fragment
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment_activity_main, fragment).commit()
    }

    // Handle the back button press
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // Handle the options item selected (hamburger menu)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
