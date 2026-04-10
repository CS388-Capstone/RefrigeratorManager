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
import androidx.fragment.app.Fragment
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
            if (barcode != null) {
                binding.etBarcode.setText(barcode)
                lookupCatalogItem(barcode)
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
            val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
            scanBarcodeLauncher.launch(intent)
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
                // Check both groupId and familyId as seen in HomeDataRepository
                groupId = data?.get("groupId") as? String ?: data?.get("familyId") as? String
                if (groupId != null) {
                    loadLocations(groupId!!)
                } else {
                    Toast.makeText(requireContext(), "No group found for user", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadLocations(groupId: String) {
        locationRepository.getGroupLocations(groupId) { locations ->
            locationsList.clear()
            val names = mutableListOf<String>()
            for (loc in locations) {
                val id = loc["id"] as? String ?: ""
                val name = loc["name"] as? String ?: "Unknown"
                locationsList.add(id to name)
                names.add(name)
            }
            
            if (isAdded) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerLocation.adapter = adapter
            }
        }
    }

    private fun lookupCatalogItem(upc: String) {
        val currentGroupId = groupId ?: return
        catalogRepository.getCatalogItem(currentGroupId, upc) { item ->
            if (item != null) {
                val name = item["name"] as? String
                if (name != null) {
                    binding.etItemName.setText(name)
                }
            }
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                binding.etExpirationDate.setText(format.format(selectedCalendar.time))
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun saveItem() {
        val currentGroupId = groupId ?: run {
            Toast.makeText(requireContext(), "Group not loaded", Toast.LENGTH_SHORT).show()
            return
        }
        val upc = binding.etBarcode.text.toString()
        val itemName = binding.etItemName.text.toString()
        val quantityStr = binding.etQuantity.text.toString()
        val expirationDateStr = binding.etExpirationDate.text.toString()

        if (upc.isBlank() || itemName.isBlank() || quantityStr.isBlank() || expirationDateStr.isBlank()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

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
                // Update or add to catalog so future scans of the same UPC auto-fill the name
                catalogRepository.addCatalogItem(currentGroupId, upc, itemName)
                Toast.makeText(requireContext(), "Item saved", Toast.LENGTH_SHORT).show()
                clearFields()
            },
            onFailure = {
                Toast.makeText(requireContext(), "Failed to save item: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun clearFields() {
        binding.etBarcode.setText("")
        binding.etItemName.setText("")
        binding.etQuantity.setText("1")
        binding.etExpirationDate.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
