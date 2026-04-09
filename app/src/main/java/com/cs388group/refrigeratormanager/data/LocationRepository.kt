package com.cs388group.refrigeratormanager.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class LocationRepository {

    private val db = Firebase.firestore

    fun addLocation(groupId: String, name: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        val location = hashMapOf("name" to name)
        db.collection("groups")
            .document(groupId)
            .collection("locations")
            .add(location)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun removeLocation(groupId: String, locationId: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        db.collection("groups")
            .document(groupId)
            .collection("locations")
            .document(locationId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getGroupLocations(groupId: String, onResult: (List<Map<String, Any>>) -> Unit) {
        db.collection("groups")
            .document(groupId)
            .collection("locations")
            .get()
            .addOnSuccessListener { snapshot ->
                val locations = snapshot.documents.map { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.id
                    data
                }
                onResult(locations)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

}
