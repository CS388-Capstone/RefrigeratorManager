package com.cs388group.refrigeratormanager

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cs388group.refrigeratormanager.databinding.ActivityMainBinding
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.cs388group.refrigeratormanager.data.FoodItem
import kotlinx.coroutines.launch
import com.cs388group.refrigeratormanager.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cs388group.refrigeratormanager.data.AppDatabase
import com.cs388group.refrigeratormanager.data.FoodItemDao

class MainActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var foodItemDao: FoodItemDao
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        foodItemDao = db.foodItemDao()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            val testItem = FoodItem(
                barcode = "123456789",
                name = "Milk",
                expirationDate = "2026-04-10",
                storageLocation = "Fridge 1",
                calories = 150
            )

            foodItemDao.insertItem(testItem)

            val allItems = foodItemDao.getAllItems()
            Log.d("DATABASE_TEST", allItems.toString())
        }

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

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_layout, fragment)
            .commit()
    }
}
