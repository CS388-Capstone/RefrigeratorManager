package com.cs388group.refrigeratormanager.fragments
import android.util.Log
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cs388group.refrigeratormanager.adapters.HomeFoodItemAdapter
import com.cs388group.refrigeratormanager.data.HomeDataRepository
import com.cs388group.refrigeratormanager.data.HomeFoodItem
import com.cs388group.refrigeratormanager.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val adapter = HomeFoodItemAdapter()
    private val homeDataRepository = HomeDataRepository()

    private var allItems: List<HomeFoodItem> = emptyList()
    private var searchQuery: String = ""
    private var selectedLocation: String = "All Locations"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        loadFoodItems()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewFoodItems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFoodItems.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchViewFood.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQuery = query.orEmpty()
                applyFilters()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText.orEmpty()
                applyFilters()
                return true
            }
        })
    }

    private fun loadFoodItems() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            _binding?.progressBarHome?.visibility = View.GONE
            _binding?.tvEmptyState?.visibility = View.VISIBLE
            _binding?.tvEmptyState?.text = "No logged in user"
            return
        }

        _binding?.progressBarHome?.visibility = View.VISIBLE
        _binding?.tvEmptyState?.visibility = View.GONE

        homeDataRepository.fetchHomeFoodItems(
            userId = currentUser.uid,
            onSuccess = { items ->
                val safeBinding = _binding ?: return@fetchHomeFoodItems

                safeBinding.progressBarHome.visibility = View.GONE
                allItems = items
                setupLocationSpinner(items)
                applyFilters()
            },
            onFailure = { e ->
                val safeBinding = _binding ?: return@fetchHomeFoodItems

                safeBinding.progressBarHome.visibility = View.GONE
                allItems = emptyList()
                setupLocationSpinner(emptyList())
                applyFilters()
                safeBinding.tvEmptyState.visibility = View.VISIBLE
                safeBinding.tvEmptyState.text = e.message ?: "Failed to load food items"
            }
        )
        Log.d("HomeDebug", "currentUser uid = ${Firebase.auth.currentUser?.uid}")
        Log.d("HomeDebug", "currentUser email = ${Firebase.auth.currentUser?.email}")
    }

    private fun setupLocationSpinner(items: List<HomeFoodItem>) {
        val safeBinding = _binding ?: return

        val locations = items.map { it.locationName }
            .distinct()
            .sorted()

        val spinnerItems = mutableListOf("All Locations")
        spinnerItems.addAll(locations)

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            spinnerItems
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        safeBinding.spinnerLocationFilter.adapter = spinnerAdapter
        safeBinding.spinnerLocationFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedLocation = spinnerItems[position]
                    applyFilters()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedLocation = "All Locations"
                    applyFilters()
                }
            }
    }

    private fun applyFilters() {
        val safeBinding = _binding ?: return

        val filtered = allItems.filter { item ->
            val matchesSearch = item.itemName.contains(searchQuery, ignoreCase = true)
            val matchesLocation = selectedLocation == "All Locations" ||
                    item.locationName == selectedLocation

            matchesSearch && matchesLocation
        }

        adapter.submitList(filtered)
        safeBinding.tvEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}