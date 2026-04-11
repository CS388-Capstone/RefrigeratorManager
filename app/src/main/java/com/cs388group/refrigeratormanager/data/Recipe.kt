package com.cs388group.refrigeratormanager.data


data class RecipeResponse(
    val recipes: List<Recipe>
)
data class Recipe(
    val name: String,
    val calories: String,
    val description: String,
    val ingredients: Map<String, String>,
    val steps: List<String>,
    val imageResId: Int
)