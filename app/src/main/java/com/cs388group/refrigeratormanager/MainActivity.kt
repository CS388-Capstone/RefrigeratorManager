package com.cs388group.refrigeratormanager

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
import com.cs388group.refrigeratormanager.activities.GroupOnboardingActivity
import com.cs388group.refrigeratormanager.activities.LoginActivity
import com.cs388group.refrigeratormanager.data.InvitationRepository
import com.cs388group.refrigeratormanager.data.InviteUI
import com.cs388group.refrigeratormanager.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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

        val invitationRepo = InvitationRepository()

        invitationRepo.sendInvitation("Vnm9VCsnxoY4GIRF9y1x", "test", "test2@example.com",
            onSuccess = {

            },
            onFailure = {

            }
        )

        if (currentFirebaseUser != null) {

            /*
            binding.btnScan.setOnClickListener {
                val intent = Intent(this, BarcodeScannerActivity::class.java)
                startActivity(intent)
            }
             */

            val homeFragment = HomeFragment()
            val scanFragment = ScanFragment()
            val genAiFragment = GenAiFragment()
            val settingsFragment = SettingsFragment()


            val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

            bottomNavigationView.setOnItemSelectedListener { item ->
                lateinit var fragment: Fragment
                when (item.itemId) {
                    R.id.menu_item_home -> fragment = homeFragment
                    R.id.menu_item_scan -> fragment = scanFragment
                    R.id.menu_item_genai -> fragment = genAiFragment
                    R.id.menu_item_settings -> fragment = settingsFragment
                }
                replaceFragment(fragment)
                true
            }

            bottomNavigationView.selectedItemId = R.id.menu_item_home
            replaceFragment(HomeFragment())

        } else { // user is not signed in
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_layout, fragment)
            .commit()
    }
}
