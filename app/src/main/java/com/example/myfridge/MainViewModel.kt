package com.example.myfridge

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myfridge.data.Item
import com.example.myfridge.data.ItemDatabase
import com.example.myfridge.data.OfflineItemRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(
    private val repository: OfflineItemRepository,
    initialWarnDays: Int
) : ViewModel() {

    val itemListState: StateFlow<List<Item>> = repository.getFridgeItemsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(100), emptyList())

    val shoppingListState: StateFlow<List<Item>> = repository.getAllItemsStream("shoppingList")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(100), emptyList())

    val savedListState: StateFlow<List<Item>> = repository.getAllItemsStream("savedItems")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(100), emptyList())

    private val _warnDays = MutableStateFlow(initialWarnDays)
    val warnDays: StateFlow<Int> = _warnDays.asStateFlow()

    // Count of fridge items expiring within the warn threshold (drives the nav badge)
    val expiringCountState: StateFlow<Int> = combine(
        repository.getFridgeItemsStream(),
        _warnDays
    ) { items, threshold ->
        items.count { it.expiryDate && it.days <= threshold }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(100), 0)

    private val _deleteEvents = MutableSharedFlow<Item>(extraBufferCapacity = 5)
    val deleteEvents = _deleteEvents.asSharedFlow()

    fun setWarnDays(days: Int) { _warnDays.value = days }

    fun addItem(type: String, name: String, expiryDate: Boolean, days: Int, count: Int) {
        val date = Calendar.getInstance().time.time
        viewModelScope.launch {
            repository.insertItem(Item(type = type, name = name, expiryDate = expiryDate, days = days, date = date, count = count))
        }
    }

    fun updateItem(item: Item, name: String, expiryDate: Boolean, days: Int, count: Int) {
        val date = Calendar.getInstance().time.time
        viewModelScope.launch {
            repository.updateItem(item.copy(name = name, expiryDate = expiryDate, days = days, date = date, count = count))
        }
    }

    fun delete(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
            _deleteEvents.emit(item)
        }
    }

    // Re-inserts the item with a new ID (id=0 lets Room auto-generate)
    fun undoDelete(item: Item) {
        viewModelScope.launch { repository.insertItem(item.copy(id = 0)) }
    }

    fun moveItemToFridge(item: Item, hasExpiry: Boolean, expiryDays: Int) {
        viewModelScope.launch {
            val date = Calendar.getInstance().time.time
            repository.insertItem(item.copy(id = 0, type = "myFridge", expiryDate = hasExpiry, days = expiryDays, date = date))
            repository.deleteItem(item)
        }
    }

    fun moveShoppingListToFridge(hasExpiry: Boolean, expiryDays: Int) {
        viewModelScope.launch {
            val date = Calendar.getInstance().time.time
            for (item in shoppingListState.value) {
                repository.insertItem(item.copy(id = 0, type = "myFridge", expiryDate = hasExpiry, days = expiryDays, date = date))
                repository.deleteItem(item)
            }
        }
    }
}

class MainViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = OfflineItemRepository(ItemDatabase.getDatabase(context).itemDao())
        val initialWarnDays = AlarmScheduler.loadWarnDays(context)
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(repository, initialWarnDays) as T
    }
}
