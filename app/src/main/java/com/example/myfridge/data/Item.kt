package com.example.myfridge.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "items")
data class Item (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
//    val user: String,
    val name: String,
    val expiryDate: Boolean,
    val days: Int = 0,
    val date: Long,
    val count: Int = 1,
)