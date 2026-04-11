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
import androidx.lifecycle.lifecycleScope
import com.cs388group.refrigeratormanager.activities.LoginActivity
import com.cs388group.refrigeratormanager.services.OpenAIService
import com.cs388group.refrigeratormanager.activities.GroupOnboardingActivity
import com.cs388group.refrigeratormanager.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private var userRepo = UserRepository()

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

        val currentUser = auth.currentUser
        // OPEN AI TEST
        Log.d("OPENAI_KEY_CHECK", "key starts with: ${BuildConfig.OPENAI_API_KEY}")
        Log.d("OPENAI_KEY_LEN", "length: ${BuildConfig.OPENAI_API_KEY.length}")
/*
        lifecycleScope.launch {
            try {
                val reply = withContext(Dispatchers.IO) {
                    var message = """
                        
                    You are a recipe generator.

                    Using the following ingredients:
                    -Salmon
                    -Rice
                    -Cheese
                    -Penne Pasta
                    -Eggs

                    Generate exactly 2 realistic, savory recipes that people would actually want to eat.

                    IMPORTANT RULES:
                    - You MUST return ONLY valid JSON. No extra text, no explanations, no markdown.
                    - The JSON format must be exactly:
                    {
                      "recipes": [
                        {
                          "name": string,
                          "calories": string,
                          "description": string,
                          "ingredients": {
                            "ingredient_name": "portion"
                          },
                          "steps": [string]
                        }
                      ]
                    }

                    REQUIREMENTS:
                    - Each recipe must include at least one key ingredient from the provided catalog.
                    - Do NOT create unrealistic combinations (e.g., apple chowder just because apples exist).
                    - You may include common seasonings, oils, and condiments even if they are not in the catalog.
                    - Do NOT include ingredients that are not realistic without the main ingredient (e.g., no lobster dishes if lobster is not in the catalog).
                    - Not all catalog ingredients must be used in every recipe.
                    - Across all recipes, try to utilize as many catalog ingredients as possible.

                    FOOD VARIETY:
                    - Include a mix of:
                      - breakfast meals
                      - lunch/dinner meals
                      - snacks
                      - pastries or baked goods

                    INGREDIENTS FORMAT:
                    - Must be a map/object:
                      "ingredients": {
                        "egg": "2",
                        "milk": "1 cup",
                        "flour": "3/4 cup"
                      }
                    - Portions should be realistic and formatted like:
                      "1 tsp", "1 cup", "1 lb", "3/4 cup", etc.

                    STEPS REQUIREMENTS:
                    - Steps must be clear, simple, and beginner-friendly.
                    - Include specific instructions (times, temperatures when relevant).
                    - Mention required tools (pan, oven, blender, etc.).
                    - If a special tool is mentioned (e.g., mixer), provide a simple alternative method.

                    CALORIES:
                    - Provide a rough estimate as a string (e.g., "450 cal").

                    DESCRIPTION:
                    - Short, appealing summary of the dish.

                    FINAL REMINDER:
                    - Output ONLY valid JSON.
                    - Do NOT include any text outside the JSON.
                    """.trimIndent()
                    OpenAIService.sendMessage(message)
                }

                Log.d("OPENAI_RESPONSE", reply)

            } catch (e: Exception) {
                Log.e("OPENAI_ERROR", "Error: ${e.message}", e)
            }
        }


 */

        if (currentUser != null) {
            val currentFirebaseUser = auth.currentUser


            if (currentFirebaseUser != null) {

                userRepo.getUser(currentFirebaseUser.uid) { userData ->
                    if (userData == null) {
                        Log.e(
                            "MainActivity",
                            "User was returned as null, this shouldn't happen since they just logged in."
                        )
                        return@getUser
                    }

                    val groupId = userData["groupId"] as? String
                    if (groupId == null) {
                        Log.w(
                            "MainActivity",
                            "User is not in a group, redirecting to Group Onboarding"
                        )
                        startActivity(Intent(this, GroupOnboardingActivity::class.java))
                        finish()
                    }
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


                val bottomNavigationView: BottomNavigationView =
                    findViewById(R.id.bottom_navigation)

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
    }
        private fun replaceFragment(fragment: Fragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame_layout, fragment)
                .commit()
        }
}

