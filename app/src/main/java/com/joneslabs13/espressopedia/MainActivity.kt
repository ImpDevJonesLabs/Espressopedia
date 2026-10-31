package com.joneslabs13.espressopedia

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.joneslabs13.espressopedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeMode = com.joneslabs13.espressopedia.data.AppPreferences.getThemeMode(this)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(themeMode)

        setContentView(R.layout.activity_main)

        // Get the NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        // Get the NavController from the NavHostFragment
        navController = navHostFragment.navController

        // Get a reference to the BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)

        // Connect the BottomNavigationView to the NavController
        bottomNavigationView.setupWithNavController(navController)
    }
}