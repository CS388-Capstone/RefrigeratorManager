package com.cs388group.refrigeratormanager.data

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore

class FoodItemRepository {

    private val db = Firebase.firestore

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
}
