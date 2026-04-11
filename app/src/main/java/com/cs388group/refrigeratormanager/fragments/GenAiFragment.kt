package com.cs388group.refrigeratormanager.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs388group.refrigeratormanager.R
import com.cs388group.refrigeratormanager.adapters.HomeFoodItemAdapter
import com.cs388group.refrigeratormanager.adapters.RecipeAdapter
import com.cs388group.refrigeratormanager.data.HomeDataRepository
import com.cs388group.refrigeratormanager.data.Recipe
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.cs388group.refrigeratormanager.databinding.FragmentGenAiBinding
import java.util.Collections.emptyList



class GenAiFragment : Fragment() {

    private lateinit var searchBar: SearchView
    private lateinit var recipeRecyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter

    private val homeDataRepository = HomeDataRepository()
    private var recipeList = mutableListOf<Recipe>(
        Recipe("Pasta", "520 cal", R.drawable.sample_food),
        Recipe("Burger", "700 cal", R.drawable.sample_food),
        Recipe("Salad", "250 cal", R.drawable.sample_food),
        Recipe("Soup", "180 cal", R.drawable.sample_food),
        Recipe("Rice Bowl", "480 cal", R.drawable.sample_food),
        Recipe("Chicken", "430 cal", R.drawable.sample_food),
        Recipe("Pasta", "520 cal", R.drawable.sample_food),
        Recipe("Burger", "700 cal", R.drawable.sample_food),
        Recipe("Salad", "250 cal", R.drawable.sample_food),
        Recipe("Soup", "180 cal", R.drawable.sample_food),
        Recipe("Rice Bowl", "480 cal", R.drawable.sample_food),
        Recipe("Chicken", "430 cal", R.drawable.sample_food)
    )

    private var _binding: FragmentGenAiBinding? = null;

    private val binding get() = _binding!!
    private val adapter = RecipeAdapter(recipeList)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenAiBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        loadFoodItems()
    }

    private fun setupRecyclerView() {

        binding.recipeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recipeRecyclerView.adapter = adapter
    }
    private fun loadFoodItems() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            return
        }


        homeDataRepository.fetchHomeFoodItems(
            userId = currentUser.uid,
            onSuccess = { items ->
                val safeBinding = _binding ?: return@fetchHomeFoodItems

                for (item in items) {
                    recipeList.add(Recipe(item.itemName, "300 cal", R.drawable.sample_food))
                }

                adapter.notifyDataSetChanged()
            },
            onFailure = { e ->
                val safeBinding = _binding ?: return@fetchHomeFoodItems

                recipeList = emptyList()
            }
        )
        Log.d("HomeDebug", "currentUser uid = ${Firebase.auth.currentUser?.uid}")
        Log.d("HomeDebug", "currentUser email = ${Firebase.auth.currentUser?.email}")
    }
}