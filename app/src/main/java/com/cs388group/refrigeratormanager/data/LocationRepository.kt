package com.cs388group.refrigeratormanager.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class LocationRepository {

    private val db = Firebase.firestore

    fun addLocation(groupId: String, locationId: String, name: String) {
        val location = hashMapOf("name" to name)
        db.collection("groups")
            .document(groupId)
            .collection("locations")
            .document(locationId)
            .set(location)
    }

    fun removeLocation(groupId: String, locationId: String) {
        db.collection("groups")
            .document(groupId)
            .collection("locations")
            .document(locationId)
            .delete()
    }

    fun getGroupLocations(groupId: String, onResult: (List<Map<String, Any>>) -> Unit) {
        db.collection("groups")
            .document(groupId)
            .collection("locations")
            .get()
            .addOnSuccessListener { snapshot ->
                val locations = snapshot.documents.map { it.data!! }
                onResult(locations)
            }
    }

}