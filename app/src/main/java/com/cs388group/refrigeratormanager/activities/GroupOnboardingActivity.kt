package com.cs388group.refrigeratormanager.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs388group.refrigeratormanager.MainActivity
import com.cs388group.refrigeratormanager.R
import com.cs388group.refrigeratormanager.adapters.InvitationAdapter
import com.cs388group.refrigeratormanager.data.GroupRepository
import com.cs388group.refrigeratormanager.data.InvitationRepository
import com.cs388group.refrigeratormanager.data.InviteUI
import com.cs388group.refrigeratormanager.data.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class GroupOnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_group_onboarding)

        val invitationRepo = InvitationRepository()
        val userRepo = UserRepository()
        val groupRepo = GroupRepository()

        val auth = Firebase.auth
        val user = auth.currentUser

        userRepo.getUser(user!!.uid) { userData ->
            if (userData == null) {
                Log.e("MainActivity", "User was returned as null, this shouldn't happen since they just logged in.")
                return@getUser
            }

            val groupId = userData["groupId"] as? String
            if (groupId == null) {
                startActivity(Intent(this, GroupOnboardingActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.group_onboarding_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerInvites)
        val adapter = InvitationAdapter(
            invites = emptyList(),
            onAccept = { invite ->
                groupRepo.addMember(invite.groupId, user!!.uid, onSuccess = {
                    invitationRepo.deleteInvitation(invite.inviteId)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }, onFailure = { e ->
                    Log.e("GroupOnboardActivity", "Error accepting group invitation", e)
                })
            },
            onDecline = { invite ->
                invitationRepo.deleteInvitation(invite.inviteId)
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)


        invitationRepo.getPendingInvites(user!!.email) { invites ->
            val invitesUI = mutableListOf<InviteUI>()
            var processed = 0

            if (invites.isEmpty()) {
                adapter.updateData(invitesUI)
                return@getPendingInvites
            }

            invites.forEach { invite ->
                val (inviteId, data) = invite
                val groupId = data["groupId"] as? String ?: ""
                val fromUserId = data["fromUserName"] as? String ?: ""

                groupRepo.getGroupName(groupId) { groupName ->
                    invitesUI.add(
                        InviteUI(
                            inviteId = inviteId,
                            groupId = groupId,
                            groupName = groupName,
                            fromUserName = fromUserId
                        )
                    )
                    processed++
                    // Only update adapter once all group names are fetched
                    if (processed == invites.size) {
                        adapter.updateData(invitesUI)
                    }
                }
            }
        }

        val groupNameText = findViewById<EditText>(R.id.etGroupName)
        val createGroupButton = findViewById<Button>(R.id.buttonCreateGroup)
        createGroupButton.setOnClickListener {
            groupRepo.createGroup( groupNameText.text.toString(), user!!.uid, onSuccess = {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, onFailure = {

            }
            )
        }



    }
}