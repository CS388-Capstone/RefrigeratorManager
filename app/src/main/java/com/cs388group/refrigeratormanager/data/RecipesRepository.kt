package com.cs388group.refrigeratormanager.data

import android.util.Log
import com.cs388group.refrigeratormanager.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

class RecipesRepository {

    private val db = Firebase.firestore

    companion object {
        private const val TAG = "RecipesRepository"
    }

    private fun getUserGroupId(
        userId: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val groupId = userDoc.getString("familyId")
                    ?: userDoc.getString("groupId")

                if (groupId.isNullOrBlank()) {
                    onFailure(Exception("User has no groupId/familyId"))
                    return@addOnSuccessListener
                }

                Log.d(TAG, "Resolved groupId=$groupId for userId=$userId")
                onSuccess(groupId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get user document for $userId", e)
                onFailure(e)
            }
    }

    fun createRecipe(
        userId: String,
        name: String,
        calories: String,
        description: String,
        ingredients: Map<String, String>,
        steps: List<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getUserGroupId(
            userId = userId,
            onSuccess = { groupId ->
                val recipeRef = db.collection("groups")
                    .document(groupId)
                    .collection("recipes")
                    .document()

                val recipeData = hashMapOf(
                    "name" to name,
                    "calories" to calories,
                    "description" to description,
                    "ingredients" to ingredients,
                    "steps" to steps,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "favorited" to false
                )

                recipeRef.set(recipeData, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "Created recipe ${name}")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to create recipe ${name}", e)
                        onFailure(e)
                    }
            },
            onFailure = { error ->
                Log.e(TAG, "error", error)

            }
        )
    }

    fun saveGeneratedRecipes(
        userId: String,
        recipes: List<Recipe>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getUserGroupId(
            userId = userId,
            onSuccess = { groupId ->
                Log.d("RecipesRepo", "Saving to groupId=$groupId")



                val first = recipes.firstOrNull()

                if (first == null) {
                    onFailure(Exception("No recipes to save"))
                    return@getUserGroupId
                }

                val recipeData = hashMapOf(
                    "name" to first.name,
                    "calories" to first.calories,
                    "description" to first.description,
                    "ingredients" to first.ingredients,
                    "steps" to first.steps
                )


                db.collection("groups")
                    .document(groupId)
                    .collection("recipes")
                    .document(first.name)
                    .set(recipeData)
            },
            onFailure = { e ->
                Log.e("RecipesRepo", "Failed to get groupId", e)
                onFailure(e)
            }
        )
    }

    fun getRecipes(
        userId: String,
        onResult: (List<Recipe>) -> Unit
    ) {
        getUserGroupId(
            userId = userId,
            onSuccess = { groupId ->
                db.collection("groups")
                    .document(groupId)
                    .collection("recipes")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val recipes = snapshot.documents.mapNotNull { doc ->
                            val name = doc.getString("name") ?: return@mapNotNull null
                            val calories = doc.getString("calories") ?: ""
                            val description = doc.getString("description") ?: ""
                            val ingredients = (doc.get("ingredients") as? Map<*, *>)
                                ?.mapNotNull { entry ->
                                    val key = entry.key as? String
                                    val value = entry.value as? String
                                    if (key != null && value != null) key to value else null
                                }
                                ?.toMap()
                                ?: emptyMap()
                            val steps = (doc.get("steps") as? List<*>)
                                ?.mapNotNull { it as? String }
                                ?: emptyList()

                            val favorited = doc.getBoolean("favorited") ?: false

                            Recipe(
                                recipeId = doc.id,
                                name = name,
                                calories = calories,
                                description = description,
                                ingredients = ingredients,
                                steps = steps,
                                imageResId = R.drawable.sample_food,
                                favorited = favorited
                            )
                        }

                        Log.d(TAG, "Fetched ${recipes.size} recipes from Firestore")
                        onResult(recipes)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to fetch recipes", e)
                        onResult(emptyList())
                    }
            },
            onFailure = {
                Log.e(TAG, "Failed to resolve groupId before fetching recipes", it)
                onResult(emptyList())
            }
        )
    }

    fun updateRecipe(
        userId: String,
        recipeId: String,
        recipe: Recipe,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getUserGroupId(
            userId = userId,
            onSuccess = { groupId ->
                val updates = hashMapOf(
                    "name" to recipe.name,
                    "calories" to recipe.calories,
                    "description" to recipe.description,
                    "ingredients" to recipe.ingredients,
                    "steps" to recipe.steps,
                    "imageResId" to recipe.imageResId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                db.collection("groups")
                    .document(groupId)
                    .collection("recipes")
                    .document(recipeId)
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(TAG, "Updated recipeId=$recipeId")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to update recipeId=$recipeId", e)
                        onFailure(e)
                    }
            },
            onFailure = onFailure
        )
    }

    fun deleteRecipe(
        userId: String,
        recipeId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getUserGroupId(
            userId = userId,
            onSuccess = { groupId ->
                db.collection("groups")
                    .document(groupId)
                    .collection("recipes")
                    .document(recipeId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Deleted recipeId=$recipeId")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to delete recipeId=$recipeId", e)
                        onFailure(e)
                    }
            },
            onFailure = onFailure
        )
    }

    fun clearRecipes(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getUserGroupId(
            userId = userId,
            onSuccess = { groupId ->
                db.collection("groups")
                    .document(groupId)
                    .collection("recipes")
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val batch = db.batch()
                        var deleteCount = 0

                        for (doc in snapshot.documents) {
                            val isFavorited = doc.getBoolean("favorited") ?: false

                            if (!isFavorited) {
                                batch.delete(doc.reference)
                                deleteCount++
                            }
                        }

                        if (deleteCount == 0) {
                            Log.d(TAG, "No non-favorited recipes to delete")
                            onSuccess()
                            return@addOnSuccessListener
                        }

                        batch.commit()
                            .addOnSuccessListener {
                                Log.d(TAG, "Deleted $deleteCount non-favorited recipes")
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to clear recipes", e)
                                onFailure(e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to fetch recipes for clearing", e)
                        onFailure(e)
                    }
            },
            onFailure = onFailure
        )
    }

    fun updateRecipeFavoriteStatus(
        userId: String,
        recipeId: String,
        favorited: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getUserGroupId(
            userId = userId,
            onSuccess = { groupId ->
                db.collection("groups")
                    .document(groupId)
                    .collection("recipes")
                    .document(recipeId)
                    .update(
                        mapOf(
                            "favorited" to favorited,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e) }
            },
            onFailure = onFailure
        )
    }
}