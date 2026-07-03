// SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
// Required Notice: Copyright (c) 2026 George Zhang — https://github.com/TheYellowDuck

package com.iamtherealgeorge.myfridge.data

import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItemByIdStream(id: Int): Flow<Item>

    fun getAllItemsStream(type: String): Flow<List<Item>>

    fun getFridgeItemsStream(): Flow<List<Item>>

    fun getExpiredItemsStream(): Flow<List<Item>>

    suspend fun insertItem(item: Item)

    suspend fun deleteItem(item: Item)

    suspend fun updateItem(item: Item)
}
