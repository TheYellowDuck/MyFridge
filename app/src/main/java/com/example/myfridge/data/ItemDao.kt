package com.example.myfridge.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Update
    suspend fun update(item: Item)

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemById(id: Int): Flow<Item>

    @Query("SELECT * FROM items WHERE type = :type ORDER BY name ASC")
    fun getAllItems(type: String): Flow<List<Item>>

    // Fridge items sorted so expiring/expired items surface first, then no-expiry items alphabetically
    @Query("""
        SELECT * FROM items WHERE type = 'myFridge'
        ORDER BY
            CASE WHEN expiryDate = 1 THEN 0 ELSE 1 END ASC,
            days ASC,
            name ASC
    """)
    fun getFridgeItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE type = 'myFridge' AND expiryDate = 1 ORDER BY days ASC")
    fun getExpiredItems(): Flow<List<Item>>
}
