package com.cs388group.refrigeratormanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cs388group.refrigeratormanager.databinding.ActivityMainBinding
import com.cs388group.refrigeratormanager.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatDelegate
import com.cs388group.refrigeratormanager.activities.GroupOnboardingActivity
import com.cs388group.refrigeratormanager.activities.LoginActivity
import com.cs388group.refrigeratormanager.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private var userRepo = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        applySettings()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val currentFirebaseUser = auth.currentUser

        if (currentFirebaseUser != null) {
            userRepo.getUser(currentFirebaseUser.uid) { userData ->
                if (userData == null) {
                    Log.e("MainActivity", "User was returned as null")
                    return@getUser
                }

                val groupId = userData["groupId"] as? String ?: userData["familyId"] as? String
                if (groupId == null) {
                    Log.w("MainActivity", "User is not in a group, redirecting to Group Onboarding")
                    startActivity(Intent(this, GroupOnboardingActivity::class.java))
                    finish()
                }
            }

            val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

            bottomNavigationView.setOnItemSelectedListener { item ->
                val currentFragment = supportFragmentManager.findFragmentById(R.id.main_frame_layout)
                
                val newFragment = when (item.itemId) {
                    R.id.menu_item_home -> if (currentFragment !is HomeFragment) HomeFragment() else null
                    R.id.menu_item_scan -> if (currentFragment !is ScanFragment) ScanFragment() else null
                    R.id.menu_item_genai -> if (currentFragment !is GenAiFragment) GenAiFragment() else null
                    R.id.menu_item_settings -> if (currentFragment !is SettingsFragment) SettingsFragment() else null
                    else -> null
                }
                
                newFragment?.let {
                    replaceFragment(it)
                }
                true
            }

            if (savedInstanceState == null) {
                // Set default fragment via listener
                bottomNavigationView.selectedItemId = R.id.menu_item_home
            }

        } else { // user is not signed in
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun applySettings() {
        val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (sharedPrefs.contains("dark_mode")) {
            val isDarkMode = sharedPrefs.getBoolean("dark_mode", false)
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_layout, fragment)
            .setReorderingAllowed(true)
            .commit()
    }
}
