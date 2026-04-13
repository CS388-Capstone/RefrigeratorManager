package com.cs388group.refrigeratormanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cs388group.refrigeratormanager.R
import com.cs388group.refrigeratormanager.databinding.FragmentFoodDetailBinding

class FoodDetailFragment : Fragment() {

    private var _binding: FragmentFoodDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val foodName = arguments?.getString("foodName") ?: "Food Item"
        val foodLocation = arguments?.getString("foodLocation") ?: "Unknown"
        val quantity = arguments?.getString("quantity") ?: "0"
        val upc = arguments?.getString("upc") ?: "N/A"
        val expirationDate = arguments?.getString("expirationDate") ?: "Unknown"

        binding.foodTitle.text = foodName
        binding.locationText.text = foodLocation
        binding.quantityText.text = quantity
        binding.upcText.text = upc
        binding.expirationText.text = expirationDate
        binding.foodImage.setImageResource(R.drawable.sample_food)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}