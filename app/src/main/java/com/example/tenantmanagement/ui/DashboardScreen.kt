package com.example.tenantmanagement.ui

import android.os.Build
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.tenantmanagement.ui.theme.AppThemeMode
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tenantmanagement.data.Building
import com.example.tenantmanagement.data.Flat
import com.example.tenantmanagement.data.Tenant
import com.example.tenantmanagement.data.TenantStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TenantViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Dashboard, 1 = Tenants, 2 = Buildings & Flats

    var selectedBuildingForFlatAdd by remember { mutableStateOf<String?>(null) }
    var showAppInfoDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentThemeMode by viewModel.themeMode.collectAsState()

    val totalNotificationCount = remember(uiState.tenants) {
        uiState.tenants.count { tenant ->
            tenant.status == TenantStatus.ACTIVE &&
                    com.example.tenantmanagement.utils.NotificationHelper.checkAnniversaryStatus(tenant).shouldShowReminder
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp)
            ) {
                // Drawer Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.height(28.dp).width(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Tenant Manager",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Properties & Rent Manager",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Items
                Text(
                    text = "NAVIGATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Tenants") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        scope.launch { drawerState.close() }
                    },
                    icon = {
                        if (totalNotificationCount > 0) {
                            BadgedBox(
                                badge = { Badge { Text("$totalNotificationCount") } }
                            ) {
                                Icon(Icons.Default.People, contentDescription = null)
                            }
                        } else {
                            Icon(Icons.Default.People, contentDescription = null)
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Buildings & Flats") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Apartment, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Theme Mode Section
                Text(
                    text = "APP THEME",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                AppThemeMode.entries.forEach { mode ->
                    NavigationDrawerItem(
                        label = { Text(mode.displayName) },
                        selected = currentThemeMode == mode,
                        onClick = {
                            viewModel.setThemeMode(context, mode)
                        },
                        icon = {
                            Icon(
                                imageVector = when (mode) {
                                    AppThemeMode.LIGHT -> Icons.Default.LightMode
                                    AppThemeMode.DARK -> Icons.Default.DarkMode
                                    AppThemeMode.SYSTEM -> Icons.Default.SettingsSuggest
                                },
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Info & Privacy Section
                Text(
                    text = "ABOUT & PRIVACY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                NavigationDrawerItem(
                    label = { Text("App & Data Info") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showAppInfoDialog = true
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu")
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    when (selectedTab) {
                                        0 -> Icons.Default.Dashboard
                                        1 -> Icons.Default.People
                                        else -> Icons.Default.Apartment
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tenant Manager",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when (selectedTab) {
                                        0 -> getFormattedTodayDate()
                                        1 -> if (totalNotificationCount > 0) "Tenants Overview • $totalNotificationCount Notice${if (totalNotificationCount > 1) "s" else ""}" else "Tenants Overview"
                                        else -> "Buildings & Properties"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            if (totalNotificationCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text("$totalNotificationCount")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.People, contentDescription = null)
                                }
                            } else {
                                Icon(Icons.Default.People, contentDescription = null)
                            }
                        },
                        label = { Text("Tenants") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Apartment, contentDescription = null) },
                        label = { Text("Buildings & Flats") }
                    )
                }
            },
            floatingActionButton = {
                if (selectedTab != 0) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (selectedTab == 1) {
                                if (uiState.buildings.isNotEmpty()) {
                                    viewModel.openAddTenantDialog()
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Please add at least one Building first before adding a tenant.",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    viewModel.openAddBuildingDialog()
                                }
                            } else if (selectedTab == 2) {
                                viewModel.openAddBuildingDialog()
                            }
                        },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text(if (selectedTab == 1) "Add Tenant" else "Add Building") }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                if (selectedTab == 0) {
                    DashboardOverviewContent(
                        uiState = uiState,
                        onNavigateToTenants = { selectedTab = 1 },
                        onNavigateToBuildings = { selectedTab = 2 }
                    )
                } else if (selectedTab == 1) {
                    if (totalNotificationCount > 0) {
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable {
                                    viewModel.onFilterSelected(FilterOption.ACTIVE)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFFED6C02),
                                    modifier = Modifier.height(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🔔 $totalNotificationCount tenant renewal notice${if (totalNotificationCount > 1) "s" else ""} pending review",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search by name, flat or aadhar...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                // Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FilterOption.entries) { filterOption ->
                        FilterChip(
                            selected = uiState.selectedFilter == filterOption,
                            onClick = { viewModel.onFilterSelected(filterOption) },
                            label = { Text(filterOption.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tenant List
                if (uiState.filteredTenants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = if (uiState.buildings.isEmpty()) "No Building Registered Yet" else if (uiState.tenants.isEmpty()) "No tenants added yet" else "No matching tenants found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (uiState.buildings.isEmpty()) "Please add at least one Building first before adding tenants." else if (uiState.tenants.isEmpty()) "Tap '+ Add Tenant' to register your first tenant." else "Try adjusting your search query or status filter.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (uiState.buildings.isEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { viewModel.openAddBuildingDialog() }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Building Now")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = uiState.filteredTenants,
                            key = { it.id }
                        ) { tenant ->
                            TenantCard(
                                tenant = tenant,
                                buildings = uiState.buildings,
                                onClick = { viewModel.openTenantDetails(tenant) },
                                onEdit = { viewModel.openEditTenantDialog(tenant) },
                                onDelete = { viewModel.deleteTenant(tenant) },
                                onCall = {
                                    if (tenant.phoneNumber.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${tenant.phoneNumber}")
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // Buildings & Properties Tab
                if (uiState.buildings.isEmpty() && uiState.flats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "No buildings or properties added yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Add Building' to create your first property.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = uiState.buildings,
                            key = { it.id }
                        ) { building ->
                            BuildingCard(
                                building = building,
                                flats = uiState.flats,
                                tenants = uiState.tenants,
                                onClick = { viewModel.openBuildingDetails(building) },
                                onEdit = { viewModel.openEditBuildingDialog(building) },
                                onDelete = { viewModel.deleteBuilding(building) },
                                onAddFlat = {
                                    selectedBuildingForFlatAdd = building.name
                                    viewModel.openAddFlatDialog()
                                }
                            )
                        }

                        // Also list standalone flats if any
                        val standaloneFlats = uiState.flats.filter { flat ->
                            uiState.buildings.none { b -> b.name.equals(flat.buildingName, ignoreCase = true) || b.id == flat.buildingId }
                        }

                        if (standaloneFlats.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Other Flats",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            items(
                                items = standaloneFlats,
                                key = { it.id }
                            ) { flat ->
                                FlatCard(
                                    flat = flat,
                                    onClick = { viewModel.openFlatDetails(flat) },
                                    onEdit = { viewModel.openEditFlatDialog(flat) },
                                    onDelete = { viewModel.deleteFlat(flat) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showAddEditDialog) {
        AddEditTenantDialog(
            tenant = uiState.tenantToEdit,
            buildings = uiState.buildings,
            flats = uiState.flats,
            tenants = uiState.tenants,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { id, name, flat, building, flatId, date, rent, advance, aadhar, status, increment, phone, email, notes, uri, existingUri, existingName ->
                viewModel.saveTenant(
                    context = context,
                    tenantId = id,
                    name = name,
                    flatNumber = flat,
                    buildingName = building,
                    flatId = flatId,
                    dateJoined = date,
                    rent = rent,
                    advanceAmount = advance,
                    aadharNumber = aadhar,
                    status = status,
                    yearlyIncrement = increment,
                    phoneNumber = phone,
                    emailAddress = email,
                    notes = notes,
                    agreementUri = uri,
                    existingAgreementUriString = existingUri,
                    existingAgreementFileName = existingName
                )
            }
        )
    }

    if (uiState.showDetailsDialog && uiState.selectedTenantForDetails != null) {
        TenantDetailDialog(
            tenant = uiState.selectedTenantForDetails!!,
            buildings = uiState.buildings,
            onDismiss = { viewModel.closeTenantDetails() },
            onEdit = {
                val t = uiState.selectedTenantForDetails!!
                viewModel.closeTenantDetails()
                viewModel.openEditTenantDialog(t)
            },
            onDelete = {
                viewModel.deleteTenant(uiState.selectedTenantForDetails!!)
            },
            onStatusChange = { newStatus ->
                viewModel.updateTenantStatus(uiState.selectedTenantForDetails!!, newStatus)
            },
            onUpdateRentToForecast = { newRent, targetYear ->
                viewModel.updateTenantRent(uiState.selectedTenantForDetails!!, newRent, targetYear)
            }
        )
    }

    if (uiState.showAddEditBuildingDialog) {
        AddEditBuildingDialog(
            building = uiState.buildingToEdit,
            onDismiss = { viewModel.closeAddEditBuildingDialog() },
            onSave = { id, name, locality, floors, ownerName, ownerEmail, notes ->
                viewModel.saveBuilding(id, name, locality, floors, ownerName, ownerEmail, notes)
            }
        )
    }

    if (uiState.showBuildingDetailsDialog && uiState.selectedBuildingForDetails != null) {
        val currentBuilding = uiState.selectedBuildingForDetails!!
        val flatsInBuilding = uiState.flats.filter {
            it.buildingId == currentBuilding.id || it.buildingName.equals(currentBuilding.name, ignoreCase = true)
        }

        BuildingDetailDialog(
            building = currentBuilding,
            flatsInBuilding = flatsInBuilding,
            onDismiss = { viewModel.closeBuildingDetails() },
            onEdit = {
                viewModel.closeBuildingDetails()
                viewModel.openEditBuildingDialog(currentBuilding)
            },
            onDelete = {
                viewModel.deleteBuilding(currentBuilding)
            },
            onAddFlatToBuilding = {
                selectedBuildingForFlatAdd = currentBuilding.name
                viewModel.openAddFlatDialog()
            },
            onSelectFlat = { flat ->
                viewModel.closeBuildingDetails()
                viewModel.openFlatDetails(flat)
            }
        )
    }

    if (uiState.showAddEditFlatDialog) {
        AddEditFlatDialog(
            flat = uiState.flatToEdit,
            buildings = uiState.buildings,
            preselectedBuildingName = selectedBuildingForFlatAdd,
            onDismiss = {
                selectedBuildingForFlatAdd = null
                viewModel.closeAddEditFlatDialog()
            },
            onSave = { id, buildingId, building, locality, flatNum, ecAcc, rrNum, waterMeter, notes ->
                selectedBuildingForFlatAdd = null
                viewModel.saveFlat(
                    flatId = id,
                    buildingId = buildingId,
                    buildingName = building,
                    locality = locality,
                    flatNumber = flatNum,
                    ecAccountNumber = ecAcc,
                    rrNumber = rrNum,
                    waterMeterNumber = waterMeter,
                    notes = notes
                )
            }
        )
    }

    if (uiState.showFlatDetailsDialog && uiState.selectedFlatForDetails != null) {
        FlatDetailDialog(
            flat = uiState.selectedFlatForDetails!!,
            onDismiss = { viewModel.closeFlatDetails() },
            onEdit = {
                val f = uiState.selectedFlatForDetails!!
                viewModel.closeFlatDetails()
                viewModel.openEditFlatDialog(f)
            },
            onDelete = {
                viewModel.deleteFlat(uiState.selectedFlatForDetails!!)
            }
        )
    }

    if (showAppInfoDialog) {
        AlertDialog(
            onDismissRequest = { showAppInfoDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .height(28.dp)
                            .width(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "App & Data Privacy Info",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhonelinkLock,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "100% Offline & Local Data Storage",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "All your data stays strictly on this phone.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    InfoPointRow(
                        icon = Icons.Default.Storage,
                        title = "Stored On Your Mobile Device",
                        description = "All tenant details, building info, flat numbers, and rent amounts are stored locally inside your phone's Room database."
                    )

                    InfoPointRow(
                        icon = Icons.Default.Folder,
                        title = "Internal Document Storage",
                        description = "Attached lease agreements and docs remain safely saved in your app's private internal storage directory."
                    )

                    InfoPointRow(
                        icon = Icons.Default.CloudOff,
                        title = "No Cloud Uploads",
                        description = "This app does not transmit, share, or upload any data to cloud servers or third parties."
                    )

                    InfoPointRow(
                        icon = Icons.Default.Shield,
                        title = "Complete Data Privacy",
                        description = "Your financial figures, contact numbers, and notes remain strictly private to you."
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ) {
                        Box(modifier = Modifier.height(0.5.dp))
                    }

                    // Developer Information Section
                    Text(
                        text = "DEVELOPER INFORMATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    InfoPointRow(
                        icon = Icons.Default.Code,
                        title = "Developed & Maintained By",
                        description = "Nayan Kumar"
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ) {
                        Box(modifier = Modifier.height(0.5.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tenant Manager v1.2",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Offline Mode",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAppInfoDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }
    }
}

@Composable
fun InfoPointRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .height(20.dp)
                .width(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BuildingCard(
    building: Building,
    flats: List<Flat>,
    tenants: List<Tenant>,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddFlat: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val buildingFlats = flats.filter { it.buildingName.equals(building.name, ignoreCase = true) || it.buildingId == building.id }
    val occupiedFlatsCount = buildingFlats.count { flat ->
        tenants.any { t -> t.flatNumber.equals(flat.flatNumber, ignoreCase = true) && t.status == TenantStatus.ACTIVE }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Apartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = building.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val subtitleText = buildString {
                            if (building.locality.isNotBlank()) append(building.locality)
                            if (building.ownerName.isNotBlank()) {
                                if (isNotEmpty()) append(" • ")
                                append("Owner: ${building.ownerName}")
                            }
                        }
                        if (subtitleText.isNotBlank()) {
                            Text(
                                text = subtitleText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Building & Flats") },
                            onClick = { showMenu = false; onClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("Add Flat to Building") },
                            onClick = { showMenu = false; onAddFlat() }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Building") },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Building") },
                            onClick = { showMenu = false; showDeleteConfirm = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${buildingFlats.size} Flats",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$occupiedFlatsCount Active Tenants",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = onAddFlat) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.height(16.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Add Flat")
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Building") },
            text = { Text("Are you sure you want to delete building '${building.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FlatCard(
    flat: Flat,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = flat.flatNumber,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = flat.buildingName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (flat.locality.isNotBlank()) {
                            Text(
                                text = flat.locality,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Details") },
                            onClick = { showMenu = false; onClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Property") },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Property") },
                            onClick = { showMenu = false; showDeleteConfirm = true }
                        )
                    }
                }
            }

            if (flat.ecAccountNumber.isNotBlank() || flat.rrNumber.isNotBlank() || flat.waterMeterNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (flat.ecAccountNumber.isNotBlank()) {
                        Column {
                            Text(
                                text = "EC Account #",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = flat.ecAccountNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (flat.rrNumber.isNotBlank()) {
                        Column {
                            Text(
                                text = "RR Number",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = flat.rrNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (flat.waterMeterNumber.isNotBlank()) {
                        Column {
                            Text(
                                text = "Water Meter #",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = flat.waterMeterNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Flat") },
            text = { Text("Are you sure you want to delete flat '${flat.flatNumber}' in ${flat.buildingName}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TenantCard(
    tenant: Tenant,
    buildings: List<Building> = emptyList(),
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCall: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val matchingBuilding = buildings.find { it.name.equals(tenant.buildingName, ignoreCase = true) }
    val ownerName = matchingBuilding?.ownerName?.trim()?.ifBlank { null } ?: "Property Owner"

    val anniversaryInfo = com.example.tenantmanagement.utils.NotificationHelper.checkAnniversaryStatus(tenant)
    val forecastRent = com.example.tenantmanagement.utils.NotificationHelper.calculateForecastRent(tenant.rent, tenant.yearlyIncrement)

    val statusColor = when (tenant.status) {
        TenantStatus.ACTIVE -> Color(0xFF2E7D32)
        TenantStatus.NOTICE_PERIOD -> Color(0xFFED6C02)
        TenantStatus.VACATED -> Color(0xFFD32F2F)
    }

    val statusBgColor = when (tenant.status) {
        TenantStatus.ACTIVE -> Color(0xFFE8F5E9)
        TenantStatus.NOTICE_PERIOD -> Color(0xFFFFF3E0)
        TenantStatus.VACATED -> Color(0xFFFFEBEE)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = tenant.flatNumber,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = tenant.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (tenant.buildingName.isNotBlank()) "${tenant.buildingName} • Joined ${tenant.dateJoined}" else "Joined: ${tenant.dateJoined}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Details") },
                            onClick = { showMenu = false; onClick() }
                        )
                        val isWhatsAppSent = isWhatsAppNoticeSent(context, tenant.id, anniversaryInfo.targetAnniversaryYear)

                        if (tenant.phoneNumber.isNotBlank() && anniversaryInfo.isAnniversaryMonth && !isWhatsAppSent) {
                            DropdownMenuItem(
                                text = { Text("Send WhatsApp Notice") },
                                onClick = {
                                    showMenu = false
                                    markWhatsAppNoticeSent(context, tenant.id, anniversaryInfo.targetAnniversaryYear)
                                    openWhatsAppNotice(context, tenant, forecastRent, ownerName)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Edit Tenant") },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Tenant") },
                            onClick = { showMenu = false; showDeleteConfirm = true }
                        )
                    }
                }
            }

            if (anniversaryInfo.shouldShowReminder && tenant.status == TenantStatus.ACTIVE) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFFED6C02),
                            modifier = Modifier.height(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (anniversaryInfo.daysRemaining > 0)
                                "🔔 Renewal Notice: Rent ₹${tenant.rent.toInt()} ➔ ₹${forecastRent.toInt()} in ~${anniversaryInfo.daysRemaining} days (${anniversaryInfo.nextAnniversaryDate})"
                            else
                                "🔔 Rent Renewal Due: Current ₹${tenant.rent.toInt()} ➔ Forecast ₹${forecastRent.toInt()} (${anniversaryInfo.nextAnniversaryDate})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            } else if (anniversaryInfo.hasBeenUpdatedThisYear && anniversaryInfo.isAnniversaryMonth && tenant.status == TenantStatus.ACTIVE) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.height(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "✓ Rent Updated: ₹${tenant.rent.toInt()}/mo for Year ${anniversaryInfo.tenancyYearNumber} (${anniversaryInfo.anniversaryMonthName} ${anniversaryInfo.targetAnniversaryYear})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rent: ₹${tenant.rent.toInt()}/mo",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Advance: ₹${tenant.advanceAmount.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tenant.agreementUri != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = CircleShape,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "Agreement attached",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.height(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Doc",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Surface(
                        color = statusBgColor,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = tenant.status.displayName,
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (tenant.phoneNumber.isNotBlank()) {
                        IconButton(onClick = onCall) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Call Tenant",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Tenant") },
            text = { Text("Are you sure you want to delete tenant '${tenant.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun getFormattedTodayDate(): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
            )
        } else {
            val sdf = java.text.SimpleDateFormat("EEEE, d MMMM yyyy", java.util.Locale.getDefault())
            sdf.format(java.util.Date())
        }
    } catch (e: Exception) {
        "Dashboard Overview"
    }
}

fun formatIndianAmount(amount: Double): String {
    val longVal = amount.toLong()
    val str = longVal.toString()
    if (str.length <= 3) return str
    val last3 = str.substring(str.length - 3)
    var rest = str.substring(0, str.length - 3)
    val sb = StringBuilder()
    while (rest.length > 2) {
        sb.insert(0, "," + rest.substring(rest.length - 2))
        rest = rest.substring(0, rest.length - 2)
    }
    return rest + sb.toString() + "," + last3
}

@Composable
fun DashboardOverviewContent(
    uiState: TenantUiState,
    onNavigateToTenants: () -> Unit,
    onNavigateToBuildings: () -> Unit
) {
    val activeCount = uiState.totalActiveTenants
    val noticeCount = uiState.tenants.count { it.status == TenantStatus.NOTICE_PERIOD }
    val vacatedCount = uiState.tenants.count { it.status == TenantStatus.VACATED }
    val buildingsCount = uiState.buildings.size
    val flatsCount = uiState.flats.size
    val monthlyRent = uiState.totalMonthlyRent
    val advanceDeposit = uiState.totalAdvanceDeposit
    val estimatedAnnualIncome = monthlyRent * 12

    LazyColumn(
        contentPadding = PaddingValues(bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // 2x2 Grid of Stat Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        title = "Active Tenants",
                        value = "$activeCount",
                        icon = Icons.Default.People,
                        containerColor = Color(0xFFE8EAF6),
                        contentColor = Color(0xFF283593),
                        onClick = onNavigateToTenants,
                        modifier = Modifier.weight(1f)
                    )

                    DashboardStatCard(
                        title = "Buildings",
                        value = "$buildingsCount",
                        icon = Icons.Default.Apartment,
                        containerColor = Color(0xFFFFF3E0),
                        contentColor = Color(0xFFE65100),
                        onClick = onNavigateToBuildings,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardStatCard(
                        title = "Monthly Income",
                        value = "₹${formatIndianAmount(monthlyRent)}",
                        icon = Icons.Default.AccountBalanceWallet,
                        containerColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF2E7D32),
                        onClick = onNavigateToTenants,
                        modifier = Modifier.weight(1f)
                    )

                    DashboardStatCard(
                        title = "Properties",
                        value = "$flatsCount",
                        icon = Icons.Default.Home,
                        containerColor = Color(0xFFE1F5FE),
                        contentColor = Color(0xFF0277BD),
                        onClick = onNavigateToBuildings,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Financial Summary Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.height(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Financial Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FinancialSummaryRow(
                        label = "Total Monthly Rent",
                        value = "₹${formatIndianAmount(monthlyRent)}",
                        icon = Icons.Default.Payments
                    )
                    FinancialSummaryRow(
                        label = "Total Advance Held",
                        value = "₹${formatIndianAmount(advanceDeposit)}",
                        icon = Icons.Default.Shield
                    )
                    FinancialSummaryRow(
                        label = "≈ Annual Income (est.)",
                        value = "₹${formatIndianAmount(estimatedAnnualIncome)}",
                        icon = Icons.Default.TrendingUp,
                        isHighlighted = true
                    )
                }
            }
        }

        // Tenant Status Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.height(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tenant Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusSummaryBadge(
                            count = activeCount,
                            label = "Active",
                            containerColor = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF2E7D32),
                            onClick = onNavigateToTenants,
                            modifier = Modifier.weight(1f)
                        )
                        StatusSummaryBadge(
                            count = noticeCount,
                            label = "Notice",
                            containerColor = Color(0xFFFFF3E0),
                            contentColor = Color(0xFFED6C02),
                            onClick = onNavigateToTenants,
                            modifier = Modifier.weight(1f)
                        )
                        StatusSummaryBadge(
                            count = vacatedCount,
                            label = "Vacated",
                            containerColor = Color(0xFFFFEBEE),
                            contentColor = Color(0xFFD32F2F),
                            onClick = onNavigateToTenants,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val valueStyle = when {
                    value.length > 9 -> MaterialTheme.typography.titleSmall
                    value.length > 6 -> MaterialTheme.typography.titleMedium
                    else -> MaterialTheme.typography.titleLarge
                }

                Text(
                    text = value,
                    style = valueStyle,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier
                            .height(20.dp)
                            .width(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FinancialSummaryRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isHighlighted: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = label,
                    style = if (isHighlighted) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold) else MaterialTheme.typography.bodyMedium,
                    color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = value,
                style = if (isHighlighted) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ) {
            Box(modifier = Modifier.height(0.5.dp))
        }
    }
}

@Composable
fun StatusSummaryBadge(
    count: Int,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}
