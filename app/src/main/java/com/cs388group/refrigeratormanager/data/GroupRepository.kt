package com.cs388group.refrigeratormanager.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.FieldValue.*
import com.google.firebase.firestore.SetOptions

class GroupRepository {

    private val db = Firebase.firestore
    private val userRepo = UserRepository()

    fun createGroup(name: String, ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val groupRef = db.collection("groups").document()
        val groupId = groupRef.id

        val groupData = hashMapOf(
            "name" to name,
            "ownerId" to ownerId,
            "members" to listOf(ownerId)
        )

        val userRef = db.collection("users").document(ownerId)

        val batch = db.batch()
        batch.set(groupRef, groupData)
        batch.update(userRef, "groupId", groupId)

        batch.commit()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun addMember(groupId: String, userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val groupRef = db.collection("groups").document(groupId)
        val userRef = db.collection("users").document(userId)

        db.runBatch { batch ->
            batch.update(groupRef, "members", arrayUnion(userId))
            batch.update(userRef, "groupId", groupId)
        }.addOnSuccessListener { onSuccess() }.addOnFailureListener { e -> onFailure(e) }
    }


    fun updateCatalogScreenshot(
        userId: String,
        data: List<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->

                val groupId = userDoc.getString("groupId")

                if (groupId.isNullOrEmpty()) {
                    onFailure(Exception("User has no groupId"))
                    return@addOnSuccessListener
                }

                val updates = hashMapOf(
                    "catalogScreenshot" to data,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                db.collection("groups")
                    .document(groupId)
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
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

    fun getGroupMembers(groupId: String, onResult: (List<String>) -> Unit) {
        db.collection("groups")
            .document(groupId)
            .collection("members")
            .get()
            .addOnSuccessListener { snapshot ->
                val members = snapshot.documents.map { it.id }
                onResult(members)
            }
    }

    fun getGroupName(groupId: String, onResult: (String) -> Unit) {
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Unknown Group"
                onResult(name)
            }
            .addOnFailureListener {
                onResult("Unknown Group")
            }
    }

    fun getGroupCatalogScreenshot(userId: String, onResult: (List<String>) -> Unit){

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val groupId = userDoc.getString("familyId")
                    ?: userDoc.getString("groupId") // fallback for current repo naming

                if (groupId.isNullOrBlank()) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }
                db.collection("groups").document(groupId)
                    .get()
                    .addOnSuccessListener { doc ->
                        val snapshot = (doc.get("catalogScreenshot") as? List<*>)
                            ?.mapNotNull { it as? String }
                            ?: emptyList()

                        onResult(snapshot)
                    }.addOnFailureListener {
                        onResult(emptyList())
                    }
            }
            .addOnFailureListener { e ->
                onResult(emptyList())
            }
    }

}