package com.cs388group.refrigeratormanager

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cs388group.refrigeratormanager.databinding.ActivityMainBinding
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.cs388group.refrigeratormanager.model.FoodItem
import kotlinx.coroutines.launch

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

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
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

        binding.btnScan.setOnClickListener {
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            startActivity(intent)
        }
    }
}
