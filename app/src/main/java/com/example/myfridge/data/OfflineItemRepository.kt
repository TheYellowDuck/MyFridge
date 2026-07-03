// SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
// Required Notice: Copyright (c) 2026 George Zhang — https://github.com/TheYellowDuck

package com.iamtherealgeorge.myfridge.data

import kotlinx.coroutines.flow.Flow

class OfflineItemRepository(private val itemDao: ItemDao) : ItemRepository {
    override fun getItemByIdStream(id: Int): Flow<Item> = itemDao.getItemById(id)

    override fun getAllItemsStream(type: String): Flow<List<Item>> = itemDao.getAllItems(type)

    override fun getFridgeItemsStream(): Flow<List<Item>> = itemDao.getFridgeItems()

    override fun getExpiredItemsStream(): Flow<List<Item>> = itemDao.getExpiredItems()

    override suspend fun insertItem(item: Item) = itemDao.insert(item)

    override suspend fun deleteItem(item: Item) = itemDao.delete(item)

    override suspend fun updateItem(item: Item) = itemDao.update(item)
}
