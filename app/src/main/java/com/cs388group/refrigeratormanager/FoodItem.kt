package com.cs388group.refrigeratormanager.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey
    val barcode: String,
    val name: String,
    val expirationDate: String,
    val storageLocation: String,
    val calories: Int
)