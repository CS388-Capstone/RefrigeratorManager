package com.cs388group.refrigeratormanager.data

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore

class FoodItemRepository {

    private val db = Firebase.firestore

    data class ExpiringFoodItem(
        val foodItemId: String,
        val locationId: String,
        val locationName: String,
        val upc: String,
        val name: String,
        val quantity: Int,
        val expirationDate: Timestamp
    )

    fun addFoodItem(
        groupId: String,
        locationId: String,
        upc: String,
        expirationDate: Timestamp,
        quantity: Int = 1,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val foodItemsRef = db.collection("groups")
            .document(groupId)
            .collection("locations")
            .document(locationId)
            .collection("foodItems")

        foodItemsRef
            .whereEqualTo("upc", upc)
            .whereEqualTo("expirationDate", expirationDate)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val item = hashMapOf(
                        "upc" to upc,
                        "expirationDate" to expirationDate,
                        "quantity" to quantity
                    )
                    foodItemsRef.add(item)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                } else {
                    val doc = snapshot.documents[0]
                    val currentQuantity = (doc.getLong("quantity") ?: 0)
                    doc.reference.update("quantity", currentQuantity + quantity)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onFailure(it) }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun removeFoodItem(groupId: String, locationId: String, foodItemId: String, quantityToRemove: Int = 1) {
        val docRef = db.collection("groups")
            .document(groupId)
            .collection("locations")
            .document(locationId)
            .collection("foodItems")
            .document(foodItemId)

        docRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val currentQuantity = doc.getLong("quantity") ?: 0
                if (currentQuantity > quantityToRemove) {
                    docRef.update("quantity", currentQuantity - quantityToRemove)
                } else {
                    docRef.delete()
                }
            }
        }
    }

    fun deleteFoodItem(groupId: String, locationId: String, foodItemId: String) {
        db.collection("groups")
            .document(groupId)
            .collection("locations")
            .document(locationId)
            .collection("foodItems")
            .document(foodItemId)
            .delete()
    }

    fun getFoodItems(groupId: String, locationId: String, onResult: (List<Map<String, Any>>) -> Unit) {
        db.collection("groups")
            .document(groupId)
            .collection("locations")
            .document(locationId)
            .collection("foodItems")
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.map { it.data!! }
                onResult(items)
            }
    }
    fun getExpiringFoodItems(
        groupId: String,
        thresholdDays: Int = 2,
        onResult: (List<ExpiringFoodItem>) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        val locationsRef = db.collection("groups")
            .document(groupId)
            .collection("locations")

        val catalogRef = db.collection("groups")
            .document(groupId)
            .collection("catalog")

        locationsRef.get()
            .addOnSuccessListener { locationSnapshot ->
                val locationDocs = locationSnapshot.documents

                if (locationDocs.isEmpty()) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                val now = Timestamp.now()
                val calendar = java.util.Calendar.getInstance().apply {
                    add(java.util.Calendar.DAY_OF_YEAR, thresholdDays)
                    set(java.util.Calendar.HOUR_OF_DAY, 23)
                    set(java.util.Calendar.MINUTE, 59)
                    set(java.util.Calendar.SECOND, 59)
                    set(java.util.Calendar.MILLISECOND, 999)
                }
                val thresholdTimestamp = Timestamp(calendar.time)

                val results = mutableListOf<ExpiringFoodItem>()
                var pendingLocationQueries = locationDocs.size
                var pendingCatalogLookups = 0
                var failed = false

                fun tryFinish() {
                    if (!failed && pendingLocationQueries == 0 && pendingCatalogLookups == 0) {
                        onResult(results)
                    }
                }

                for (locationDoc in locationDocs) {
                    val locationId = locationDoc.id
                    val locationName = locationDoc.getString("name") ?: "Unknown Location"

                    locationsRef.document(locationId)
                        .collection("foodItems")
                        .whereGreaterThanOrEqualTo("expirationDate", now)
                        .whereLessThanOrEqualTo("expirationDate", thresholdTimestamp)
                        .get()
                        .addOnSuccessListener { foodSnapshot ->
                            if (foodSnapshot.isEmpty) {
                                pendingLocationQueries--
                                tryFinish()
                                return@addOnSuccessListener
                            }

                            for (foodDoc in foodSnapshot.documents) {
                                val upc = foodDoc.getString("upc") ?: continue
                                val quantity = foodDoc.getLong("quantity")?.toInt() ?: 1
                                val expirationDate = foodDoc.getTimestamp("expirationDate") ?: continue

                                pendingCatalogLookups++

                                catalogRef.document(upc)
                                    .get()
                                    .addOnSuccessListener { catalogDoc ->
                                        val name = catalogDoc.getString("name") ?: upc

                                        results.add(
                                            ExpiringFoodItem(
                                                foodItemId = foodDoc.id,
                                                locationId = locationId,
                                                locationName = locationName,
                                                upc = upc,
                                                name = name,
                                                quantity = quantity,
                                                expirationDate = expirationDate
                                            )
                                        )

                                        pendingCatalogLookups--
                                        tryFinish()
                                    }
                                    .addOnFailureListener { e ->
                                        if (!failed) {
                                            failed = true
                                            onFailure(e)
                                        }
                                    }
                            }

                            pendingLocationQueries--
                            tryFinish()
                        }
                        .addOnFailureListener { e ->
                            if (!failed) {
                                failed = true
                                onFailure(e)
                            }
                        }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }
}
