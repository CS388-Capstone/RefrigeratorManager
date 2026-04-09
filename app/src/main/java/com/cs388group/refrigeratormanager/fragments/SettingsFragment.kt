package com.cs388group.refrigeratormanager.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs388group.refrigeratormanager.R
import com.cs388group.refrigeratormanager.activities.LoginActivity
import com.cs388group.refrigeratormanager.data.LocationRepository
import com.cs388group.refrigeratormanager.data.UserRepository
import com.cs388group.refrigeratormanager.databinding.FragmentSettingsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val userRepository = UserRepository()
    private val locationRepository = LocationRepository()
    private var groupId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        binding.btnAddLocation.setOnClickListener {
            val name = binding.etLocationName.text.toString()
            if (name.isNotBlank() && groupId != null) {
                locationRepository.addLocation(groupId!!, name, onSuccess = {
                    binding.etLocationName.setText("")
                    loadLocations(groupId!!)
                }, onFailure = {
                    Toast.makeText(requireContext(), "Failed to add location", Toast.LENGTH_SHORT).show()
                })
            }
        }

        binding.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
        
        binding.rvLocations.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadUserData() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            userRepository.getUser(user.uid) { data ->
                groupId = data?.get("groupId") as? String ?: data?.get("familyId") as? String
                if (groupId != null) {
                    loadLocations(groupId!!)
                }
            }
        }
    }

    private fun loadLocations(groupId: String) {
        locationRepository.getGroupLocations(groupId) { locations ->
            if (isAdded && _binding != null) {
                binding.rvLocations.adapter = LocationAdapter(locations) { locationId ->
                    locationRepository.removeLocation(groupId, locationId, onSuccess = {
                        loadLocations(groupId)
                    })
                }
            }
        }
    }

    private inner class LocationAdapter(
        private val locations: List<Map<String, Any>>,
        private val onDeleteClick: (String) -> Unit
    ) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameText: TextView = view.findViewById(android.R.id.text1)
            val deleteText: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val loc = locations[position]
            holder.nameText.text = loc["name"] as? String ?: "Unknown"
            holder.deleteText.text = "Tap to delete"
            holder.itemView.setOnClickListener {
                val id = loc["id"] as? String
                if (id != null) {
                    onDeleteClick(id)
                }
            }
        }

        override fun getItemCount() = locations.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
