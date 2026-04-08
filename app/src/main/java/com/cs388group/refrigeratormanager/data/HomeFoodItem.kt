package com.cs388group.refrigeratormanager.data

data class HomeFoodItem(
    val foodItemId: String,
    val catalogItemId: String,
    val itemName: String,
    val expirationDateText: String,
    val quantity: Int,
    val locationId: String,
    val locationName: String
)