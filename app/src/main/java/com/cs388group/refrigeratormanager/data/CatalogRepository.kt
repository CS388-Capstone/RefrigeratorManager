package com.cs388group.refrigeratormanager.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class CatalogRepository {

    private val db = Firebase.firestore

    fun addCatalogItem(groupId: String, upc: String, name: String, calories: Int?) {
        val item = hashMapOf(
            "name" to name,
            "upc" to upc,
            "calories" to calories
        )
        db.collection("groups")
            .document(groupId)
            .collection("catalog")
            .document(upc)
            .set(item)
    }

    fun removeCatalogItem(groupId: String, upc: String) {
        db.collection("groups")
            .document(groupId)
            .collection("catalog")
            .document(upc)
            .delete()
    }

    fun getCatalogItems(groupId: String, onResult: (List<Map<String, Any>>) -> Unit) {
        db.collection("groups")
            .document(groupId)
            .collection("catalog")
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.map { it.data!! }
                onResult(items)
            }
    }

    fun getCatalogItem(groupId: String, upc: String, onResult: (Map<String, Any>?) -> Unit) {
        db.collection("groups")
            .document(groupId)
            .collection("catalog")
            .document(upc)
            .get()
            .addOnSuccessListener { doc ->
                onResult(doc.data)
            }
            .addOnFailureListener { onResult(null) }
    }

}
