package com.cs388group.refrigeratormanager.data

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log

class HomeDataRepository {

    private val db = Firebase.firestore

    fun fetchHomeFoodItems(
        userId: String,
        onSuccess: (List<HomeFoodItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val groupId = userDoc.getString("familyId")
                    ?: userDoc.getString("groupId") // fallback for current repo naming
                Log.d("HomeDebug", "userDoc id = ${userDoc.id}")
                Log.d("HomeDebug", "familyId = ${userDoc.getString("familyId")}")
                Log.d("HomeDebug", "groupId = ${userDoc.getString("groupId")}")

                if (groupId.isNullOrBlank()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                loadCatalog(groupId,
                    onSuccess = { catalogMap ->
                        loadLocationsAndFoodItems(groupId, catalogMap, onSuccess, onFailure)
                    },
                    onFailure = onFailure
                )
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun loadCatalog(
        groupId: String,
        onSuccess: (Map<String, String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("groups")
            .document(groupId)
            .collection("catalog")
            .get()
            .addOnSuccessListener { snapshot ->
                val catalogMap = snapshot.documents.associate { doc ->
                    val name = doc.getString("name") ?: "Unknown Item"
                    doc.id to name
                }
                onSuccess(catalogMap)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun loadLocationsAndFoodItems(
        groupId: String,
        catalogMap: Map<String, String>,
        onSuccess: (List<HomeFoodItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("groups")
            .document(groupId)
            .collection("locations")
            .get()
            .addOnSuccessListener { locationsSnapshot ->
                val locationDocs = locationsSnapshot.documents

                if (locationDocs.isEmpty()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }

                val allItems = mutableListOf<HomeFoodItem>()
                var remaining = locationDocs.size
                var failed = false

                for (locationDoc in locationDocs) {
                    val locationId = locationDoc.id
                    val locationName = locationDoc.getString("name") ?: "Unknown Location"

                    db.collection("groups")
                        .document(groupId)
                        .collection("locations")
                        .document(locationId)
                        .collection("foodItems")
                        .get()
                        .addOnSuccessListener { foodSnapshot ->
                            for (foodDoc in foodSnapshot.documents) {
                                val catalogItemId =
                                    foodDoc.getString("catalogItemId")
                                        ?: foodDoc.getString("upc")
                                        ?: ""

                                val itemName = catalogMap[catalogItemId] ?: "Unknown Item"

                                val expirationTimestamp =
                                    foodDoc.getTimestamp("expirationDate")

                                val quantity = (foodDoc.getLong("quantity") ?: 1L).toInt()

                                allItems.add(
                                    HomeFoodItem(
                                        foodItemId = foodDoc.id,
                                        catalogItemId = catalogItemId,
                                        itemName = itemName,
                                        expirationDateText = formatTimestamp(expirationTimestamp),
                                        quantity = quantity,
                                        locationId = locationId,
                                        locationName = locationName
                                    )
                                )
                            }

                            remaining--
                            if (remaining == 0 && !failed) {
                                val sorted = allItems.sortedBy { it.itemName.lowercase() }
                                onSuccess(sorted)
                            }
                        }
                        .addOnFailureListener { e ->
                            if (!failed) {
                                failed = true
                                onFailure(e)
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    private fun formatTimestamp(timestamp: Timestamp?): String {
        if (timestamp == null) return "No expiration date"
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(timestamp.toDate())
    }
}