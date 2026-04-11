package com.cs388group.refrigeratormanager.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.cs388group.refrigeratormanager.R
import com.cs388group.refrigeratormanager.adapters.RecipeAdapter
import com.cs388group.refrigeratormanager.data.GroupRepository
import com.cs388group.refrigeratormanager.data.HomeDataRepository
import com.cs388group.refrigeratormanager.data.HomeFoodItem
import com.cs388group.refrigeratormanager.data.Recipe
import com.cs388group.refrigeratormanager.data.RecipeResponse
import com.cs388group.refrigeratormanager.data.RecipesRepository
import com.cs388group.refrigeratormanager.databinding.FragmentGenAiBinding
import com.cs388group.refrigeratormanager.services.OpenAIService
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GenAiFragment : Fragment() {

    private var _binding: FragmentGenAiBinding? = null
    private val binding get() = _binding!!

    private val homeDataRepository = HomeDataRepository()
    private val groupRepository = GroupRepository()
    private val recipesRepository = RecipesRepository()

    private val recipeList = mutableListOf<Recipe>()
    private lateinit var adapter: RecipeAdapter

    private var foodItems: List<HomeFoodItem> = emptyList()
    private var currCatalog: List<String> = emptyList()

    companion object {
        private const val TAG = "GenAiFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadRecipes()

        binding.refreshRecipesButton.setOnClickListener {
            val currentUser = Firebase.auth.currentUser ?: return@setOnClickListener

            val latestFoodNames = foodItems
                .map { it.itemName.trim() }
                .filter { it.isNotBlank() }

            if (latestFoodNames.isNotEmpty()) {
                currCatalog = latestFoodNames
                updateCatalog(currentUser.uid)
                generateRecipes(currCatalog)
            } else {
                loadRecipes()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter(recipeList)
        binding.recipeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recipeRecyclerView.adapter = adapter
    }

    private fun loadRecipes() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No current user")
            return
        }

        Log.d(TAG, "Starting loadRecipes for user=${currentUser.uid}")

        recipesRepository.getRecipes(currentUser.uid) { existingRecipes ->
            if (_binding == null) return@getRecipes

            Log.d(TAG, "Existing recipes count = ${existingRecipes.size}")

            // Show cached/saved recipes immediately if they exist
            if (existingRecipes.isNotEmpty()) {
                recipeList.clear()
                recipeList.addAll(existingRecipes)
                adapter.notifyDataSetChanged()
                Log.d(TAG, "Displayed existing recipes")
            }

            loadFoodCatalogAndMaybeGenerate(currentUser.uid, existingRecipes.isNotEmpty())
        }
    }

    private fun loadFoodCatalogAndMaybeGenerate(userId: String, hadExistingRecipes: Boolean) {
        homeDataRepository.fetchHomeFoodItems(
            userId = userId,
            onSuccess = { items ->
                if (_binding == null) return@fetchHomeFoodItems

                Log.d(TAG, "Fetched food items count = ${items.size}")

                foodItems = items

                val currentFoodNames = foodItems
                    .map { it.itemName.trim() }
                    .filter { it.isNotBlank() }

                Log.d(TAG, "Current food names = $currentFoodNames")

                if (currentFoodNames.isEmpty()) {
                    Log.w(TAG, "No food items found, not generating recipes")
                    return@fetchHomeFoodItems
                }

                groupRepository.getGroupCatalogScreenshot(
                    userId = userId,
                    onResult = { catalog ->
                        if (_binding == null) return@getGroupCatalogScreenshot

                        currCatalog = catalog
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        Log.d(TAG, "Stored catalog = $currCatalog")

                        val shouldGenerate = when {
                            currCatalog.isEmpty() -> {
                                Log.d(TAG, "Catalog is empty, should generate")
                                currCatalog = currentFoodNames
                                updateCatalog(userId)
                                true
                            }

                            else -> {
                                val diff = calculateDifference(currentFoodNames, currCatalog)
                                Log.d(TAG, "Catalog diff = $diff")

                                if (diff >= 2) {
                                    Log.d(TAG, "Diff >= 2, should generate")
                                    currCatalog = currentFoodNames
                                    updateCatalog(userId)
                                    true
                                } else {
                                    Log.d(TAG, "Diff < 2, will not regenerate")
                                    false
                                }
                            }
                        }

                        if (shouldGenerate) {
                            generateRecipes(currCatalog)
                        } else {
                            if (hadExistingRecipes) {
                                Log.d(TAG, "Keeping existing recipes")
                            } else {
                                Log.d(TAG, "No existing recipes and no regeneration triggered")
                            }
                        }
                    }
                )
            },
            onFailure = { e ->
                Log.e(TAG, "Failed to load food items", e)
            }
        )
    }

    private fun updateCatalog(userId: String) {
        groupRepository.updateCatalogScreenshot(
            userId = userId,
            data = currCatalog,
            onSuccess = {
                Log.d(TAG, "Catalog updated successfully")
            },
            onFailure = { e ->
                Log.e(TAG, "Catalog update failed", e)
            }
        )
    }

    private fun generateRecipes(catalog: List<String>) {
        if (catalog.isEmpty()) {
            Log.w(TAG, "generateRecipes called with empty catalog")
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val catalogString = catalog.joinToString("\n") { "- $it" }
                Log.d("OPENAI_INPUT", catalogString)

                val prompt = """
                    You are a recipe generator.
                    
                    Using the following ingredients:
                    $catalogString
                    
                    Generate exactly 1 realistic, savory recipes that people would actually want to eat.

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

                val reply = withContext(Dispatchers.IO) {
                    OpenAIService.sendMessage(prompt)
                }

                Log.d("OPENAI_RESPONSE", reply)
                handleRecipeResponse(reply)

            } catch (e: Exception) {
                Log.e("OPENAI_ERROR", "Error generating recipes", e)
            }
        }
    }

    private fun handleRecipeResponse(reply: String) {
        try {
            val curUser = Firebase.auth.currentUser
            Log.d("GenAiAuth", "currentUser = ${curUser?.uid}")
            Log.d("GenAiAuth", "email = ${curUser?.email}")
            Log.d("GenAiAuth", "isAnonymous = ${curUser?.isAnonymous}")
            val cleaned = reply
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            Log.d(TAG, "Cleaned response = $cleaned")

            val parsed = Gson().fromJson(cleaned, RecipeResponse::class.java)

            if (parsed?.recipes.isNullOrEmpty()) {
                Log.e(TAG, "Parsed recipe response is empty")
                return
            }

            val appRecipes = parsed.recipes.map {
                Recipe(
                    name = it.name,
                    calories = it.calories,
                    description = it.description,
                    ingredients = it.ingredients,
                    steps = it.steps,
                    imageResId = R.drawable.sample_food
                )
            }

            Log.d(TAG, "Parsed appRecipes count = ${appRecipes.size}")

            recipeList.clear()
            recipeList.addAll(appRecipes)
            adapter.notifyDataSetChanged()

            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                recipesRepository.saveGeneratedRecipes(
                    userId = currentUser.uid,
                    recipes = appRecipes,
                    onSuccess = {
                        Log.d(TAG, "Saved generated recipes")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Failed to save generated recipes", e)
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("GenAiParse", "Parse error", e)
        }
    }

    private fun calculateDifference(newList: List<String>, oldList: List<String>): Int {
        val matched = mutableSetOf<Int>()
        var diff = 0

        for (newItem in newList) {
            var found = false

            for ((i, oldItem) in oldList.withIndex()) {
                if (i in matched) continue

                if (isSimilar(newItem, oldItem)) {
                    matched.add(i)
                    found = true
                    break
                }
            }

            if (!found) diff++
        }

        val removed = oldList.size - matched.size
        return diff + removed
    }

    private fun isSimilar(a: String, b: String): Boolean {
        val s1 = a.lowercase().trim()
        val s2 = b.lowercase().trim()

        if (s1 == s2) return true
        if (s1.contains(s2) || s2.contains(s1)) return true

        return levenshteinDistance(s1, s2) <= 2
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }

        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        return dp[a.length][b.length]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}