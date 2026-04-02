package com.cs388group.refrigeratormanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cs388group.refrigeratormanager.data.FoodItem

@Dao
interface FoodItemDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertItem(item: FoodItem)

    @Update
    suspend fun updateItem(item: FoodItem)

    @Delete
    suspend fun deleteItem(item: FoodItem)

    @Query("SELECT * FROM food_items")
    suspend fun getAllItems(): List<FoodItem>

    @Query("SELECT * FROM food_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getItemByBarcode(barcode: String): FoodItem?
}