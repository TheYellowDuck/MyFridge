package com.example.myfridge

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myfridge.reusables.EditCard
import com.example.myfridge.reusables.ItemCard
import com.example.myfridge.reusables.MoveToFridgeDialog
import com.example.myfridge.reusables.TopBar
import com.example.myfridge.ui.theme.MyFridgeTheme

sealed class Screens(val route: String) {
    object MyFridge : Screens("myFridge")
    object SavedItems : Screens("savedItems")
    object ShoppingList : Screens("shoppingList")
    object Settings : Screens("settings")
}

enum class SortMode { Expiry, Alphabetical }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.POST_NOTIFICATIONS"), 1)
        }
        AlarmScheduler.scheduleDaily(this)
        setContent {
            MyFridgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 3.dp
                ) {
                    MainActivityScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityScreen() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel(
        LocalViewModelStoreOwner.current!!,
        "MainViewModel",
        MainViewModelFactory(LocalContext.current)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val expiringCount by viewModel.expiringCountState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect delete events and show an undo snackbar
    LaunchedEffect(viewModel) {
        viewModel.deleteEvents.collect { deletedItem ->
            val result = snackbarHostState.showSnackbar(
                message = "${deletedItem.name} deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete(deletedItem)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Screens.ShoppingList.route,
                    onClick = {
                        navController.navigate(Screens.ShoppingList.route) {
                            popUpTo(Screens.MyFridge.route)
                        }
                    },
                    icon = { Icon(painterResource(R.drawable.shopping_cart), contentDescription = null) },
                    label = { Text("Shopping") }
                )
                NavigationBarItem(
                    selected = currentRoute == Screens.MyFridge.route || currentRoute == null,
                    onClick = {
                        navController.navigate(Screens.MyFridge.route) {
                            popUpTo(Screens.MyFridge.route)
                        }
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (expiringCount > 0) {
                                    Badge { Text(expiringCount.toString()) }
                                }
                            }
                        ) {
                            Icon(painterResource(R.drawable.logo), contentDescription = null)
                        }
                    },
                    label = { Text("My Fridge") }
                )
                NavigationBarItem(
                    selected = currentRoute == Screens.SavedItems.route,
                    onClick = {
                        navController.navigate(Screens.SavedItems.route) {
                            popUpTo(Screens.MyFridge.route)
                        }
                    },
                    icon = { Icon(painterResource(R.drawable.bookmarks), contentDescription = null) },
                    label = { Text("Saved") }
                )
            }
        }
    ) { contentPadding ->
        NavHost(navController = navController, startDestination = Screens.MyFridge.route) {
            composable(Screens.MyFridge.route) { MyFridge(contentPadding, viewModel, navController) }
            composable(Screens.SavedItems.route) { SavedItems(contentPadding, viewModel, navController) }
            composable(Screens.ShoppingList.route) { ShoppingList(contentPadding, viewModel, navController) }
            composable(Screens.Settings.route) { Settings(contentPadding, navController, viewModel) }
        }
    }
}

@Composable
private fun EmptyState(iconRes: Int, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MyFridge(contentPadding: PaddingValues, viewModel: MainViewModel, navController: NavController) {
    val itemList by viewModel.itemListState.collectAsState()
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(SortMode.Expiry) }
    var showSortMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val filteredList = if (searchQuery.isBlank()) itemList
        else itemList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val displayList = if (sortMode == SortMode.Alphabetical)
        filteredList.sortedBy { it.name.lowercase() } else filteredList

    Column(modifier = Modifier.padding(contentPadding)) {
        TopBar(
            navController = navController,
            title = { Text("My Fridge", style = MaterialTheme.typography.titleLarge) },
            actionButton = {
                Row {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sort by expiry") },
                                leadingIcon = if (sortMode == SortMode.Expiry) ({
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }) else null,
                                onClick = { sortMode = SortMode.Expiry; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort A–Z") },
                                leadingIcon = if (sortMode == SortMode.Alphabetical) ({
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }) else null,
                                onClick = { sortMode = SortMode.Alphabetical; showSortMenu = false }
                            )
                        }
                    }
                    IconButton(onClick = {
                        searchExpanded = !searchExpanded
                        if (!searchExpanded) { searchQuery = ""; focusManager.clearFocus() }
                    }) {
                        Icon(
                            imageVector = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchExpanded) "Close search" else "Search"
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screens.Settings.route) }) {
                        Icon(painterResource(R.drawable.settings), contentDescription = "Settings")
                    }
                }
            }
        )
        AnimatedVisibility(visible = searchExpanded) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search items…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        LazyColumn(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentPadding = PaddingValues(bottom = 10.dp)
        ) {
            when {
                itemList.isEmpty() -> item { EmptyState(R.drawable.logo, "Your fridge is empty — add an item below.") }
                displayList.isEmpty() -> item {
                    Text(
                        "No items match \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            items(displayList, key = { it.id }) { item ->
                ItemCard(viewModel = viewModel, item = item)
            }
            item { EditCard(viewModel = viewModel, type = "myFridge") }
        }
    }
}

@Composable
fun SavedItems(contentPadding: PaddingValues, viewModel: MainViewModel, navController: NavController) {
    val itemList by viewModel.savedListState.collectAsState()
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredList = if (searchQuery.isBlank()) itemList
        else itemList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.padding(contentPadding)) {
        TopBar(
            navController = navController,
            title = { Text("Saved Items", style = MaterialTheme.typography.titleLarge) },
            actionButton = {
                Row {
                    IconButton(onClick = {
                        searchExpanded = !searchExpanded
                        if (!searchExpanded) { searchQuery = ""; focusManager.clearFocus() }
                    }) {
                        Icon(
                            imageVector = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchExpanded) "Close search" else "Search"
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screens.Settings.route) }) {
                        Icon(painterResource(R.drawable.settings), contentDescription = "Settings")
                    }
                }
            }
        )
        AnimatedVisibility(visible = searchExpanded) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search items…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        LazyColumn(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentPadding = PaddingValues(bottom = 10.dp)
        ) {
            when {
                itemList.isEmpty() -> item { EmptyState(R.drawable.bookmarks, "No saved items yet — add a template below.") }
                filteredList.isEmpty() -> item {
                    Text(
                        "No items match \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            items(filteredList, key = { it.id }) { item ->
                ItemCard(viewModel = viewModel, item = item)
            }
            item { EditCard(viewModel = viewModel, type = "savedItems") }
        }
    }
}

@Composable
fun ShoppingList(contentPadding: PaddingValues, viewModel: MainViewModel, navController: NavController) {
    val itemList by viewModel.shoppingListState.collectAsState()
    var searchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredList = if (searchQuery.isBlank()) itemList
        else itemList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val focusManager = LocalFocusManager.current
    var showMoveAllDialog by remember { mutableStateOf(false) }

    if (showMoveAllDialog) {
        MoveToFridgeDialog(
            title = "Move all to My Fridge",
            subtitle = "${itemList.size} item${if (itemList.size == 1) "" else "s"} — set expiry:",
            onConfirm = { hasExpiry, days ->
                viewModel.moveShoppingListToFridge(hasExpiry, days)
                showMoveAllDialog = false
            },
            onDismiss = { showMoveAllDialog = false }
        )
    }

    Column(modifier = Modifier.padding(contentPadding)) {
        TopBar(
            navController = navController,
            title = { Text("Shopping List", style = MaterialTheme.typography.titleLarge) },
            actionButton = {
                Row {
                    IconButton(onClick = {
                        searchExpanded = !searchExpanded
                        if (!searchExpanded) { searchQuery = ""; focusManager.clearFocus() }
                    }) {
                        Icon(
                            imageVector = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchExpanded) "Close search" else "Search"
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screens.Settings.route) }) {
                        Icon(painterResource(R.drawable.settings), contentDescription = "Settings")
                    }
                }
            }
        )
        AnimatedVisibility(visible = searchExpanded) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search items…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        LazyColumn(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentPadding = PaddingValues(bottom = 10.dp)
        ) {
            // "Move all to fridge" bulk action
            if (itemList.isNotEmpty()) {
                item {
                    Button(
                        onClick = { showMoveAllDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Move all to My Fridge")
                    }
                }
            }
            when {
                itemList.isEmpty() -> item { EmptyState(R.drawable.shopping_cart, "Your shopping list is empty.") }
                filteredList.isEmpty() -> item {
                    Text(
                        "No items match \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            items(filteredList, key = { it.id }) { item ->
                ItemCard(viewModel = viewModel, item = item)
            }
            item { EditCard(viewModel = viewModel, type = "shoppingList") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(contentPadding: PaddingValues, navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val (savedHour, savedMinute) = AlarmScheduler.loadPrefs(context)
    var displayHour by remember { mutableStateOf(savedHour) }
    var displayMinute by remember { mutableStateOf(savedMinute) }
    var timeConfirmed by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = savedHour,
        initialMinute = savedMinute,
        is24Hour = true
    )
    var warnDays by remember { mutableStateOf(AlarmScheduler.loadWarnDays(context)) }
    var notificationsEnabled by remember { mutableStateOf(AlarmScheduler.loadNotificationsEnabled(context)) }

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Notification time",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                    TimePicker(state = timePickerState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = {
                            AlarmScheduler.savePrefs(context, timePickerState.hour, timePickerState.minute)
                            AlarmScheduler.scheduleDaily(context, force = true)
                            displayHour = timePickerState.hour
                            displayMinute = timePickerState.minute
                            timeConfirmed = true
                            showTimePicker = false
                        }) { Text("OK") }
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.padding(contentPadding)) {
        TopBar(
            navController = navController,
            title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
            actionButton = {}
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Notifications ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Notifications", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        notificationsEnabled = it
                        AlarmScheduler.saveNotificationsEnabled(context, it)
                    }
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Daily time to check expiry and send alerts.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (notificationsEnabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            AnimatedVisibility(visible = notificationsEnabled) {
                Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "%02d:%02d".format(displayHour, displayMinute),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.width(16.dp))
                Button(onClick = { timeConfirmed = false; showTimePicker = true }) {
                    Text("Change")
                }
            }
            if (timeConfirmed) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Saved! Next check at %02d:%02d.".format(displayHour, displayMinute),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(16.dp))
            // ── Expiry warning threshold ───────────────────────────────────
            Text("Expiry warning", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(
                "Notify when an item expires within this many days.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (warnDays > 0) {
                            warnDays--
                            AlarmScheduler.saveWarnDays(context, warnDays)
                            viewModel.setWarnDays(warnDays)
                        }
                    }
                ) {
                    Icon(painterResource(R.drawable.remove), contentDescription = "Decrease")
                }
                Text(
                    "$warnDays day${if (warnDays == 1) "" else "s"}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = {
                        if (warnDays < 30) {
                            warnDays++
                            AlarmScheduler.saveWarnDays(context, warnDays)
                            viewModel.setWarnDays(warnDays)
                        }
                    }
                ) {
                    Icon(painterResource(R.drawable.add), contentDescription = "Increase")
                }
            }
                } // close Column
            } // close AnimatedVisibility

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(24.dp))

            // ── About ──────────────────────────────────────────────────────
            Text("About", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "My Fridge — track what's in your fridge and get notified before items expire.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
