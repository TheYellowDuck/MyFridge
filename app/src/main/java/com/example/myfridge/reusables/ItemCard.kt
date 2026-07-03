// SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
// Required Notice: Copyright (c) 2026 George Zhang — https://github.com/TheYellowDuck

package com.iamtherealgeorge.myfridge.reusables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.iamtherealgeorge.myfridge.MainViewModel
import com.iamtherealgeorge.myfridge.R
import com.iamtherealgeorge.myfridge.data.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCard(viewModel: MainViewModel, item: Item) {
    var editing by remember { mutableStateOf(false) }
    var count by remember { mutableStateOf(if (item.type == "shoppingList") item.count.toString() else "0") }
    val focusManager = LocalFocusManager.current

    val dismissState = rememberDismissState(
        confirmValueChange = { value ->
            if (value != DismissValue.Default) {
                viewModel.delete(item)
                true
            } else false
        }
    )

    if (!editing) {
        SwipeToDismiss(
            state = dismissState,
            modifier = Modifier.padding(bottom = 10.dp),
            directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
            background = {
                val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                val color by animateColorAsState(
                    targetValue = when (dismissState.targetValue) {
                        DismissValue.Default -> Color.Transparent
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    label = "swipe_bg"
                )
                val alignment = if (direction == DismissDirection.StartToEnd)
                    Alignment.CenterStart else Alignment.CenterEnd
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            },
            dismissContent = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editing = true },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.type == "shoppingList") {
                                    TextField(
                                        value = count,
                                        onValueChange = { count = it },
                                        placeholder = { Text("0") },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = {
                                            focusManager.clearFocus()
                                            val qty = count.toIntOrNull() ?: 1
                                            viewModel.updateItem(item, item.name, item.expiryDate, item.days, qty)
                                        }),
                                        maxLines = 1,
                                        modifier = Modifier.width(75.dp),
                                        textStyle = MaterialTheme.typography.titleMedium
                                    )
                                    Text("  ×  ", style = MaterialTheme.typography.titleMedium)
                                }
                                Text(
                                    text = item.name,
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .horizontalScroll(rememberScrollState()),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            if (item.expiryDate && item.type != "shoppingList") {
                                ExpiryLabel(item.days)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (item.type) {
                                "myFridge" -> MyFridgeActions(viewModel, item, count)
                                "savedItems" -> SavedItemActions(viewModel, item, count, focusManager)
                                "shoppingList" -> ShoppingListActions(viewModel, item)
                            }
                        }
                    }
                }
            }
        )
    } else {
        EditCard(viewModel = viewModel, item = item, type = item.type, saveEdit = { editing = false })
    }
}

@Composable
private fun ExpiryLabel(days: Int) {
    val subtleColor = MaterialTheme.colorScheme.onSurfaceVariant
    val warningColor = Color(0xFFE65100)
    val errorColor = MaterialTheme.colorScheme.error

    when {
        days > 1 -> Text(
            "Expires in $days days",
            color = subtleColor,
            style = MaterialTheme.typography.bodyMedium
        )
        days == 1 -> Text(
            "Expires tomorrow",
            color = subtleColor,
            style = MaterialTheme.typography.bodyMedium
        )
        days == 0 -> Text(
            "Expires today",
            color = warningColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        else -> Text(
            "Expired ${-days} day${if (-days == 1) "" else "s"} ago",
            color = errorColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MyFridgeActions(viewModel: MainViewModel, item: Item, count: String) {
    if (item.days >= 0 || !item.expiryDate) {
        if (item.count == 1) {
            IconButton(onClick = { viewModel.delete(item) }) {
                Icon(painterResource(R.drawable.delete), contentDescription = "Delete")
            }
        } else {
            IconButton(onClick = {
                viewModel.updateItem(item, item.name, item.expiryDate, item.days, item.count - 1)
            }) {
                Icon(painterResource(R.drawable.remove), contentDescription = "Remove one")
            }
        }
        Text("${item.count}", style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = {
            viewModel.updateItem(item, item.name, item.expiryDate, item.days, item.count + 1)
        }) {
            Icon(painterResource(R.drawable.add), contentDescription = "Add one")
        }
    } else {
        // Item has expired — offer to move to shopping list or delete
        IconButton(onClick = {
            val qty = count.toIntOrNull() ?: 0
            if (qty > 0) {
                viewModel.addItem("shoppingList", item.name, item.expiryDate, item.days, qty)
            }
        }) {
            Icon(painterResource(R.drawable.shopping_cart_checkout), contentDescription = "Add to shopping list")
        }
        IconButton(onClick = { viewModel.delete(item) }) {
            Icon(painterResource(R.drawable.delete), contentDescription = "Delete")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingListActions(viewModel: MainViewModel, item: Item) {
    var showMoveDialog by remember { mutableStateOf(false) }
    if (showMoveDialog) {
        MoveToFridgeDialog(
            subtitle = item.name,
            initialHasExpiry = item.expiryDate,
            initialDays = item.days.coerceAtLeast(0),
            onConfirm = { hasExpiry, days ->
                viewModel.moveItemToFridge(item, hasExpiry, days)
                showMoveDialog = false
            },
            onDismiss = { showMoveDialog = false }
        )
    }
    IconButton(onClick = { showMoveDialog = true }) {
        Icon(painterResource(R.drawable.logo), contentDescription = "Move to fridge")
    }
    IconButton(onClick = { viewModel.delete(item) }) {
        Icon(painterResource(R.drawable.shopping_cart_remove), contentDescription = "Remove from list")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveToFridgeDialog(
    title: String = "Move to My Fridge",
    subtitle: String? = null,
    initialHasExpiry: Boolean = true,
    initialDays: Int = 7,
    onConfirm: (hasExpiry: Boolean, days: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hasExpiry by remember { mutableStateOf(initialHasExpiry) }
    var daysText by remember { mutableStateOf(initialDays.toString()) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Checkbox(checked = hasExpiry, onCheckedChange = { hasExpiry = it })
                    Text("Has expiry date", style = MaterialTheme.typography.bodyMedium)
                }
                AnimatedVisibility(visible = hasExpiry) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    ) {
                        Text("Expires in ", style = MaterialTheme.typography.bodyMedium)
                        TextField(
                            value = daysText,
                            onValueChange = { daysText = it },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            modifier = Modifier.width(75.dp),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        Text(" days", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val days = if (hasExpiry) (daysText.toIntOrNull() ?: 7).coerceAtLeast(0) else 0
                onConfirm(hasExpiry, days)
            }) { Text("Move") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SavedItemActions(
    viewModel: MainViewModel,
    item: Item,
    count: String,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    var localCount by remember { mutableStateOf(count) }
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Count: ", style = MaterialTheme.typography.bodyMedium)
            TextField(
                value = localCount,
                onValueChange = { localCount = it },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                maxLines = 1,
                modifier = Modifier.width(75.dp),
                textStyle = MaterialTheme.typography.titleMedium
            )
        }
        Row {
            IconButton(onClick = {
                val qty = localCount.toIntOrNull() ?: 0
                if (qty > 0) viewModel.addItem("myFridge", item.name, item.expiryDate, item.days, qty)
                focusManager.clearFocus()
            }) {
                Icon(painterResource(R.drawable.logo), contentDescription = "Add to fridge")
            }
            IconButton(onClick = {
                val qty = localCount.toIntOrNull() ?: 0
                if (qty > 0) viewModel.addItem("shoppingList", item.name, item.expiryDate, item.days, qty)
                focusManager.clearFocus()
            }) {
                Icon(painterResource(R.drawable.shopping_cart_checkout), contentDescription = "Add to shopping list")
            }
            IconButton(onClick = {
                viewModel.delete(item)
                focusManager.clearFocus()
            }) {
                Icon(painterResource(R.drawable.bookmark_remove), contentDescription = "Remove saved item")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCard(viewModel: MainViewModel, item: Item? = null, type: String, saveEdit: () -> Unit = {}) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var days by remember { mutableStateOf(item?.days?.toString() ?: "0") }
    var count by remember { mutableStateOf(item?.count?.toString() ?: "1") }
    var expiryDate by remember { mutableStateOf(item?.expiryDate ?: true) }
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 20.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item == null) "Add Item" else "Edit Item",
                    style = MaterialTheme.typography.titleLarge
                )
                if (item != null) {
                    IconButton(onClick = saveEdit) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel edit")
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalAlignment = Alignment.Start
                ) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Name") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        textStyle = MaterialTheme.typography.titleMedium
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (type != "shoppingList") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text("Expiry Date", style = MaterialTheme.typography.titleMedium)
                                Checkbox(checked = expiryDate, onCheckedChange = { expiryDate = it })
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text("Expires in ", style = MaterialTheme.typography.titleMedium)
                                TextField(
                                    enabled = expiryDate,
                                    value = days,
                                    onValueChange = { days = it },
                                    placeholder = { Text("0") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    maxLines = 1,
                                    modifier = Modifier.width(75.dp),
                                    textStyle = MaterialTheme.typography.titleMedium
                                )
                                Text(" days", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        if (type != "savedItems") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Count: ", style = MaterialTheme.typography.titleMedium)
                                TextField(
                                    value = count,
                                    onValueChange = { count = it },
                                    placeholder = { Text("1") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    maxLines = 1,
                                    modifier = Modifier.width(75.dp),
                                    textStyle = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                IconButton(
                    onClick = {
                        val daysVal = days.toIntOrNull() ?: 0
                        val countVal = count.toIntOrNull() ?: 1
                        if (name.isNotBlank() && (type == "savedItems" || countVal > 0)) {
                            if (item == null) {
                                viewModel.addItem(type, name.trim(), expiryDate, daysVal, countVal)
                            } else {
                                viewModel.updateItem(item, name.trim(), expiryDate, daysVal, countVal)
                                saveEdit()
                            }
                        }
                    }
                ) {
                    Icon(painterResource(R.drawable.done), contentDescription = "Save")
                }
            }
        }
    }
}
