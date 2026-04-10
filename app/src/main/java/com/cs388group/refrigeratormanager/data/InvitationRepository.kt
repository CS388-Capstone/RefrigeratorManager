package com.cs388group.refrigeratormanager.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class InvitationRepository {

    private val db = Firebase.firestore

    fun sendInvitation(
        groupId: String,
        fromUserName: String,
        toUserEmail: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val invite = hashMapOf(
            "groupId" to groupId,
            "fromUserName" to fromUserName,
            "toUserEmail" to toUserEmail,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("invitations")
            .add(invite)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e("InvitationRepo", "Failed to send invite", e)
                onFailure(e)
            }
    }

    fun getPendingInvites(
        userEmail: String?,
        onResult: (List<Pair<String, Map<String, Any>>>) -> Unit
    ) {
        if (userEmail == null) {
            onResult(emptyList())
            return
        }

        db.collection("invitations")
            .whereEqualTo("toUserEmail", userEmail)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                val invites = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { Pair(doc.id, it) }
                }
                onResult(invites)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun deleteInvitation(invitationId: String) {
        db.collection("invitations")
            .document(invitationId)
            .delete()
    }

}