package com.cs388group.refrigeratormanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cs388group.refrigeratormanager.R
import com.cs388group.refrigeratormanager.databinding.FragmentRecipeDetailBinding

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipeName = arguments?.getString("recipeName") ?: "Recipe"
        val ingredients = arguments?.getString("ingredientsText") ?: "No ingredients"
        val steps = arguments?.getString("stepsText") ?: "No steps"

        binding.recipeTitle.text = recipeName
        binding.ingredientsText.text = ingredients
        binding.stepsText.text = steps
        binding.recipeImage.setImageResource(R.drawable.sample_food)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}