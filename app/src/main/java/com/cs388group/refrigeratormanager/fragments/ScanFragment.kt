package com.cs388group.refrigeratormanager.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.cs388group.refrigeratormanager.BarcodeScannerActivity
import com.cs388group.refrigeratormanager.data.*
import com.cs388group.refrigeratormanager.databinding.FragmentScanBinding
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.*

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private val userRepository = UserRepository()
    private val locationRepository = LocationRepository()
    private val catalogRepository = CatalogRepository()
    private val foodItemRepository = FoodItemRepository()

    private var groupId: String? = null
    private val locationsList = mutableListOf<Pair<String, String>>() // Pair(id, name)

    private var selectedCalendar = Calendar.getInstance()

    private val scanBarcodeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val barcode = result.data?.getStringExtra("SCAN_RESULT")
            _binding?.let { b ->
                if (barcode != null) {
                    b.etBarcode.setText(barcode)
                    // lookupCatalogItem is now handled by the text watcher on etBarcode
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        binding.btnOpenScanner.setOnClickListener {
            if (isAdded && viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
                scanBarcodeLauncher.launch(intent)
            }
        }

        binding.etBarcode.doAfterTextChanged { text ->
            val barcode = text?.toString()?.trim()
            // Standard UPC barcodes are usually 8, 12, or 13 digits
            if (!barcode.isNullOrBlank() && (barcode.length == 8 || barcode.length == 12 || barcode.length == 13)) {
                lookupCatalogItem(barcode)
            }
        }

        binding.etExpirationDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSaveItem.setOnClickListener {
            saveItem()
        }
    }

    private fun loadUserData() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            userRepository.getUser(user.uid) { data ->
                if (!isAdded) return@getUser
                groupId = data?.get("groupId") as? String ?: data?.get("familyId") as? String
                if (groupId != null) {
                    loadLocations(groupId!!)
                } else {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "No group found for user", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadLocations(groupId: String) {
        locationRepository.getGroupLocations(groupId) { locations ->
            if (!isAdded) return@getGroupLocations
            locationsList.clear()
            val names = mutableListOf<String>()
            for (loc in locations) {
                val id = loc["id"] as? String ?: ""
                val name = loc["name"] as? String ?: "Unknown"
                locationsList.add(id to name)
                names.add(name)
            }
            
            _binding?.let { b ->
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                b.spinnerLocation.adapter = adapter
            }
        }
    }

    private fun lookupCatalogItem(upc: String) {
        val currentGroupId = groupId ?: return
        catalogRepository.getCatalogItem(currentGroupId, upc) { item ->
            if (item != null && _binding != null) {
                val name = item["name"] as? String
                val calories = item["calories"]?.toString()
                if (name != null) {
                    binding.etItemName.setText(name)
                }
                if (calories != null) {
                    binding.etCalories.setText(calories)
                }
            }
        }
    }

    private fun showDatePicker() {
        val context = context ?: return
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                _binding?.etExpirationDate?.setText(format.format(selectedCalendar.time))
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveItem() {
        val currentGroupId = groupId ?: run {
            if (isAdded) Toast.makeText(requireContext(), "Group not loaded", Toast.LENGTH_SHORT).show()
            return
        }
        val upc = binding.etBarcode.text.toString()
        val itemName = binding.etItemName.text.toString()
        val caloriesStr = binding.etCalories.text.toString()
        val quantityStr = binding.etQuantity.text.toString()
        val expirationDateStr = binding.etExpirationDate.text.toString()

        if (upc.isBlank() || itemName.isBlank() || quantityStr.isBlank() || expirationDateStr.isBlank()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val calories = caloriesStr.toIntOrNull()
        val quantity = quantityStr.toIntOrNull() ?: 1
        val expirationDate = Timestamp(selectedCalendar.time)

        val selectedPosition = binding.spinnerLocation.selectedItemPosition
        if (selectedPosition < 0 || selectedPosition >= locationsList.size) {
            Toast.makeText(requireContext(), "Please select a location", Toast.LENGTH_SHORT).show()
            return
        }
        val locationId = locationsList[selectedPosition].first

        foodItemRepository.addFoodItem(currentGroupId, locationId, upc, expirationDate, quantity,
            onSuccess = {
                if (isAdded) {
                    catalogRepository.addCatalogItem(currentGroupId, upc, itemName, calories)
                    Toast.makeText(requireContext(), "Item saved", Toast.LENGTH_SHORT).show()
                    clearFields()
                }
            },
            onFailure = {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed to save item: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun clearFields() {
        _binding?.let { b ->
            b.etBarcode.setText("")
            b.etItemName.setText("")
            b.etCalories.setText("")
            b.etQuantity.setText("1")
            b.etExpirationDate.setText("")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
