package com.cs388group.refrigeratormanager.fragments

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs388group.refrigeratormanager.activities.GroupOnboardingActivity
import com.cs388group.refrigeratormanager.activities.LoginActivity
import com.cs388group.refrigeratormanager.data.GroupRepository
import com.cs388group.refrigeratormanager.data.InvitationRepository
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
    private val groupRepository = GroupRepository()
    private val invitationRepository = InvitationRepository()
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

        setupDarkModeSwitch()
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

        binding.btnInviteMember.setOnClickListener {
            val email = binding.etInvitationEmail.text.toString()
            val user = Firebase.auth.currentUser
            userRepository.getUser(user!!.uid,
                onResult = { user ->
                    val currentUserName = user!!["displayName"] as? String ?: "Unknown"
                    invitationRepository.sendInvitation(groupId!!, currentUserName, email,
                        onSuccess = {
                            binding.etInvitationEmail.setText("")
                            Toast.makeText(requireContext(), "Invitation sent", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            Log.e("SettingsFragment", "Failed to send invitation", it)
                        })
                })
        }

        binding.btnLeaveGroup.setOnClickListener {
            val user = Firebase.auth.currentUser
            if (user != null) {
                groupRepository.removeMember(groupId!!, user.uid)
                startActivity(Intent(requireContext(), GroupOnboardingActivity::class.java))
                requireActivity().finish()
            }
        }

        binding.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
        
        binding.rvLocations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGroup.layoutManager = LinearLayoutManager(requireContext())
        
        setupGroupSwipe()
    }

    private fun setupDarkModeSwitch() {
        val sharedPrefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("dark_mode", (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
        
        binding.switchDarkMode.isChecked = isDarkMode
        
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun loadUserData() {
        val user = Firebase.auth.currentUser
        if (user != null) {
            userRepository.getUser(user.uid) { data ->
                groupId = data?.get("groupId") as? String ?: data?.get("familyId") as? String
                if (groupId != null) {
                    loadLocations(groupId!!)
                    loadGroupMembers(groupId!!)
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

    private fun loadGroupMembers(groupId: String) {
        groupRepository.getGroupMembers(groupId,
            onResult = { members ->
                if (isAdded && _binding != null) {
                    binding.rvGroup.adapter = GroupMemberAdapter(members, Firebase.auth.currentUser?.uid)
                }
            })
    }

    private fun setupGroupSwipe() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val adapter = binding.rvGroup.adapter as? GroupMemberAdapter ?: return
                val memberId = adapter.getMemberId(position)

                if (groupId != null) {
                    groupRepository.removeMember(groupId!!, memberId)
                    loadGroupMembers(groupId!!)
                    Toast.makeText(requireContext(), "Member removed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val position = viewHolder.adapterPosition
                val adapter = binding.rvGroup.adapter as? GroupMemberAdapter ?: return 0
                val memberId = adapter.getMemberId(position)
                
                // Don't allow swiping the current user
                if (memberId == Firebase.auth.currentUser?.uid) return 0
                
                return super.getSwipeDirs(recyclerView, viewHolder)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvGroup)
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

    private inner class GroupMemberAdapter(
        private val members: List<String>,
        private val currentUserId: String?
    ) : RecyclerView.Adapter<GroupMemberAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameText: TextView = view.findViewById(android.R.id.text1)
            val deleteText: TextView = view.findViewById(android.R.id.text2)
        }

        fun getMemberId(position: Int) = members[position]

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val memberId = members[position]
            
            // Initial state
            holder.nameText.text = "Loading..."
            holder.deleteText.text = ""
            holder.itemView.setOnClickListener(null)
            
            userRepository.getUser(memberId,
                onResult = { member ->
                    holder.nameText.text = member?.get("displayName") as? String ?: "Name Unknown"
                    if (memberId == currentUserId) {
                        holder.deleteText.text = "You"
                    } else {
                        holder.deleteText.text = "Member (Slide to remove)"
                    }
                }
            )
        }

        override fun getItemCount() = members.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
