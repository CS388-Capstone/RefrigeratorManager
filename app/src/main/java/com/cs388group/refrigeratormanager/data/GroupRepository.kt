package com.cs388group.refrigeratormanager.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.FieldValue.*

class GroupRepository {

    private val db = Firebase.firestore

    fun createGroup(groupId: String, name: String, ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val groupData = hashMapOf(
            "name" to name,
            "ownerId" to ownerId,
            "members" to listOf(ownerId)
        )
        db.collection("groups").document(groupId)
            .set(groupData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun addMember(groupId: String, userId: String) {
        val groupRef = db.collection("groups").document(groupId)
        groupRef.update("members", arrayUnion(userId))
    }

    fun removeMember(groupId: String, userId: String) {
        val groupRef = db.collection("groups").document(groupId)
        groupRef.update("members", arrayRemove(userId))
    }

    fun deleteGroup(groupId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("groups").document(groupId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

}