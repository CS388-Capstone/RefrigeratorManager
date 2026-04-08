package com.cs388group.refrigeratormanager.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserRepository {

    private val db = Firebase.firestore

    fun createUser(uid: String, displayName: String, email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userData = hashMapOf(
            "displayName" to displayName,
            "email" to email,
            "groupId" to null
        )
        db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getUser(uid: String, onResult: (Map<String, Any>?) -> Unit) {
          db.collection("users").document(uid)
              .get()
              .addOnSuccessListener { doc ->
                  onResult(doc.data)
              }
              .addOnFailureListener { onResult(null) }
    }

    fun updateGroup(uid: String, groupId: String?) {
        db.collection("users").document(uid)
            .update("groupId", groupId)
    }

}