package com.example.tenantmanagement.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tenantmanagement.data.Building
import com.example.tenantmanagement.data.Flat
import com.example.tenantmanagement.data.Tenant
import com.example.tenantmanagement.data.TenantRepository
import com.example.tenantmanagement.data.TenantStatus
import com.example.tenantmanagement.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class TenantUiState(
    val tenants: List<Tenant> = emptyList(),
    val filteredTenants: List<Tenant> = emptyList(),
    val flats: List<Flat> = emptyList(),
    val buildings: List<Building> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: FilterOption = FilterOption.ACTIVE,
    val totalActiveTenants: Int = 0,
    val totalMonthlyRent: Double = 0.0,
    val totalAdvanceDeposit: Double = 0.0,
    val selectedTenantForDetails: Tenant? = null,
    val tenantToEdit: Tenant? = null,
    val showAddEditDialog: Boolean = false,
    val showDetailsDialog: Boolean = false,
    val showAddEditFlatDialog: Boolean = false,
    val flatToEdit: Flat? = null,
    val selectedFlatForDetails: Flat? = null,
    val showFlatDetailsDialog: Boolean = false,
    val showAddEditBuildingDialog: Boolean = false,
    val buildingToEdit: Building? = null,
    val selectedBuildingForDetails: Building? = null,
    val showBuildingDetailsDialog: Boolean = false
)

enum class FilterOption(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    NOTICE_PERIOD("Notice"),
    VACATED("Vacated")
}

class TenantViewModel(private val repository: TenantRepository) : ViewModel() {

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode

    fun initThemeMode(context: Context) {
        val prefs = context.getSharedPreferences("tenant_mgmt_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name)
        val mode = try { AppThemeMode.valueOf(saved ?: "") } catch (e: Exception) { AppThemeMode.SYSTEM }
        _themeMode.value = mode
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        _themeMode.value = mode
        val prefs = context.getSharedPreferences("tenant_mgmt_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow(FilterOption.ACTIVE)
    val selectedFilter: StateFlow<FilterOption> = _selectedFilter

    private val _selectedTenantForDetails = MutableStateFlow<Tenant?>(null)
    private val _tenantToEdit = MutableStateFlow<Tenant?>(null)
    private val _showAddEditDialog = MutableStateFlow(false)
    private val _showDetailsDialog = MutableStateFlow(false)

    private val _showAddEditFlatDialog = MutableStateFlow(false)
    private val _flatToEdit = MutableStateFlow<Flat?>(null)
    private val _selectedFlatForDetails = MutableStateFlow<Flat?>(null)
    private val _showFlatDetailsDialog = MutableStateFlow(false)

    private val _showAddEditBuildingDialog = MutableStateFlow(false)
    private val _buildingToEdit = MutableStateFlow<Building?>(null)
    private val _selectedBuildingForDetails = MutableStateFlow<Building?>(null)
    private val _showBuildingDetailsDialog = MutableStateFlow(false)

    private data class SearchFilterState(val query: String, val filter: FilterOption)
    private data class TenantDialogState(
        val selectedForDetails: Tenant?,
        val toEdit: Tenant?,
        val showAddEdit: Boolean,
        val showDetails: Boolean
    )
    private data class FlatDialogState(
        val showAddEditFlat: Boolean,
        val flatToEdit: Flat?,
        val selectedFlatForDetails: Flat?,
        val showFlatDetails: Boolean
    )
    private data class BuildingDialogState(
        val showAddEditBuilding: Boolean,
        val buildingToEdit: Building?,
        val selectedBuildingForDetails: Building?,
        val showBuildingDetails: Boolean
    )

    private val _searchFilterState = combine(_searchQuery, _selectedFilter) { query, filter ->
        SearchFilterState(query, filter)
    }

    private val _tenantDialogState = combine(
        _selectedTenantForDetails,
        _tenantToEdit,
        _showAddEditDialog,
        _showDetailsDialog
    ) { selectedForDetails, toEdit, showAddEdit, showDetails ->
        TenantDialogState(selectedForDetails, toEdit, showAddEdit, showDetails)
    }

    private val _flatDialogState = combine(
        _showAddEditFlatDialog,
        _flatToEdit,
        _selectedFlatForDetails,
        _showFlatDetailsDialog
    ) { showAddEditFlat, flatToEdit, selectedFlatForDetails, showFlatDetails ->
        FlatDialogState(showAddEditFlat, flatToEdit, selectedFlatForDetails, showFlatDetails)
    }

    private val _buildingDialogState = combine(
        _showAddEditBuildingDialog,
        _buildingToEdit,
        _selectedBuildingForDetails,
        _showBuildingDetailsDialog
    ) { showAddEditBuilding, buildingToEdit, selectedBuildingForDetails, showBuildingDetails ->
        BuildingDialogState(showAddEditBuilding, buildingToEdit, selectedBuildingForDetails, showBuildingDetails)
    }

    val uiState: StateFlow<TenantUiState> = combine(
        repository.allTenants,
        repository.allFlats,
        repository.allBuildings,
        _searchFilterState,
        _tenantDialogState
    ) { tenants, flats, buildings, searchFilter, tenantDialogs ->
        Tuple5(tenants, flats, buildings, searchFilter, tenantDialogs)
    }.combine(_flatDialogState) { tuple5, flatDialogs ->
        Tuple6(tuple5.a, tuple5.b, tuple5.c, tuple5.d, tuple5.e, flatDialogs)
    }.combine(_buildingDialogState) { tuple6, buildingDialogs ->
        val tenants = tuple6.a
        val flats = tuple6.b
        val buildings = tuple6.c
        val searchFilter = tuple6.d
        val tenantDialogs = tuple6.e
        val flatDialogs = tuple6.f

        val query = searchFilter.query
        val filter = searchFilter.filter

        val filtered = tenants.filter { tenant ->
            val matchesQuery = tenant.name.contains(query, ignoreCase = true) ||
                    tenant.flatNumber.contains(query, ignoreCase = true) ||
                    tenant.buildingName.contains(query, ignoreCase = true) ||
                    tenant.aadharNumber.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                FilterOption.ALL -> true
                FilterOption.ACTIVE -> tenant.status == TenantStatus.ACTIVE
                FilterOption.NOTICE_PERIOD -> tenant.status == TenantStatus.NOTICE_PERIOD
                FilterOption.VACATED -> tenant.status == TenantStatus.VACATED
            }

            matchesQuery && matchesFilter
        }

        val activeTenants = tenants.filter { it.status == TenantStatus.ACTIVE }
        val activeCount = activeTenants.size
        val monthlyRentSum = activeTenants.sumOf { it.rent }
        val advanceSum = activeTenants.sumOf { it.advanceAmount }

        TenantUiState(
            tenants = tenants,
            filteredTenants = filtered,
            flats = flats,
            buildings = buildings,
            searchQuery = query,
            selectedFilter = filter,
            totalActiveTenants = activeCount,
            totalMonthlyRent = monthlyRentSum,
            totalAdvanceDeposit = advanceSum,
            selectedTenantForDetails = tenantDialogs.selectedForDetails,
            tenantToEdit = tenantDialogs.toEdit,
            showAddEditDialog = tenantDialogs.showAddEdit,
            showDetailsDialog = tenantDialogs.showDetails,
            showAddEditFlatDialog = flatDialogs.showAddEditFlat,
            flatToEdit = flatDialogs.flatToEdit,
            selectedFlatForDetails = flatDialogs.selectedFlatForDetails,
            showFlatDetailsDialog = flatDialogs.showFlatDetails,
            showAddEditBuildingDialog = buildingDialogs.showAddEditBuilding,
            buildingToEdit = buildingDialogs.buildingToEdit,
            selectedBuildingForDetails = buildingDialogs.selectedBuildingForDetails,
            showBuildingDetailsDialog = buildingDialogs.showBuildingDetails
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TenantUiState(selectedFilter = FilterOption.ACTIVE)
    )

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
    private data class Tuple6<A, B, C, D, E, F>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: FilterOption) {
        _selectedFilter.value = filter
    }

    fun openAddTenantDialog() {
        _tenantToEdit.value = null
        _showAddEditDialog.value = true
    }

    fun openEditTenantDialog(tenant: Tenant) {
        _tenantToEdit.value = tenant
        _showAddEditDialog.value = true
    }

    fun closeAddEditDialog() {
        _showAddEditDialog.value = false
        _tenantToEdit.value = null
    }

    fun openTenantDetails(tenant: Tenant) {
        _selectedTenantForDetails.value = tenant
        _showDetailsDialog.value = true
    }

    fun closeTenantDetails() {
        _showDetailsDialog.value = false
        _selectedTenantForDetails.value = null
    }

    // Building Management
    fun openAddBuildingDialog() {
        _buildingToEdit.value = null
        _showAddEditBuildingDialog.value = true
    }

    fun openEditBuildingDialog(building: Building) {
        _buildingToEdit.value = building
        _showAddEditBuildingDialog.value = true
    }

    fun closeAddEditBuildingDialog() {
        _showAddEditBuildingDialog.value = false
        _buildingToEdit.value = null
    }

    fun openBuildingDetails(building: Building) {
        _selectedBuildingForDetails.value = building
        _showBuildingDetailsDialog.value = true
    }

    fun closeBuildingDetails() {
        _showBuildingDetailsDialog.value = false
        _selectedBuildingForDetails.value = null
    }

    fun saveBuilding(
        buildingId: Int,
        name: String,
        locality: String,
        totalFloors: String,
        ownerName: String = "",
        ownerEmail: String = "",
        notes: String
    ) {
        viewModelScope.launch {
            val building = Building(
                id = buildingId,
                name = name,
                locality = locality,
                totalFloors = totalFloors,
                ownerName = ownerName,
                ownerEmail = ownerEmail,
                notes = notes
            )
            if (buildingId == 0) {
                repository.insertBuilding(building)
            } else {
                repository.updateBuilding(building)
            }
            closeAddEditBuildingDialog()
        }
    }

    fun deleteBuilding(building: Building) {
        viewModelScope.launch {
            repository.deleteBuilding(building)
            closeBuildingDetails()
        }
    }

    // Flat Dialog Management
    fun openAddFlatDialog() {
        _flatToEdit.value = null
        _showAddEditFlatDialog.value = true
    }

    fun openEditFlatDialog(flat: Flat) {
        _flatToEdit.value = flat
        _showAddEditFlatDialog.value = true
    }

    fun closeAddEditFlatDialog() {
        _showAddEditFlatDialog.value = false
        _flatToEdit.value = null
    }

    fun openFlatDetails(flat: Flat) {
        _selectedFlatForDetails.value = flat
        _showFlatDetailsDialog.value = true
    }

    fun closeFlatDetails() {
        _showFlatDetailsDialog.value = false
        _selectedFlatForDetails.value = null
    }

    fun saveFlat(
        flatId: Int,
        buildingId: Int = 0,
        buildingName: String,
        locality: String,
        flatNumber: String,
        ecAccountNumber: String,
        rrNumber: String,
        waterMeterNumber: String,
        notes: String
    ) {
        viewModelScope.launch {
            val flat = Flat(
                id = flatId,
                buildingId = buildingId,
                buildingName = buildingName,
                locality = locality,
                flatNumber = flatNumber,
                ecAccountNumber = ecAccountNumber,
                rrNumber = rrNumber,
                waterMeterNumber = waterMeterNumber,
                notes = notes
            )
            if (flatId == 0) {
                repository.insertFlat(flat)
            } else {
                repository.updateFlat(flat)
            }
            closeAddEditFlatDialog()
        }
    }

    fun deleteFlat(flat: Flat) {
        viewModelScope.launch {
            repository.deleteFlat(flat)
            closeFlatDetails()
        }
    }

    fun saveTenant(
        context: Context,
        tenantId: Int,
        name: String,
        flatNumber: String,
        buildingName: String = "",
        flatId: Int? = null,
        dateJoined: String,
        rent: Double,
        advanceAmount: Double,
        aadharNumber: String,
        status: TenantStatus,
        yearlyIncrement: String,
        phoneNumber: String,
        emailAddress: String = "",
        notes: String,
        agreementUri: Uri?,
        existingAgreementUriString: String?,
        existingAgreementFileName: String?
    ) {
        viewModelScope.launch {
            var finalUriString = existingAgreementUriString
            var finalFileName = existingAgreementFileName

            if (agreementUri != null) {
                val copiedInfo = copyFileToInternalStorage(context, agreementUri)
                if (copiedInfo != null) {
                    finalUriString = copiedInfo.first
                    finalFileName = copiedInfo.second
                }
            }

            val tenant = Tenant(
                id = tenantId,
                name = name,
                flatNumber = flatNumber,
                buildingName = buildingName,
                flatId = flatId,
                dateJoined = dateJoined,
                rent = rent,
                advanceAmount = advanceAmount,
                aadharNumber = aadharNumber,
                status = status,
                yearlyIncrement = yearlyIncrement,
                phoneNumber = phoneNumber,
                emailAddress = emailAddress,
                notes = notes,
                agreementUri = finalUriString,
                agreementFileName = finalFileName
            )

            if (tenantId == 0) {
                repository.insertTenant(tenant)
            } else {
                repository.updateTenant(tenant)
            }

            closeAddEditDialog()
        }
    }

    fun deleteTenant(tenant: Tenant) {
        viewModelScope.launch {
            repository.deleteTenant(tenant)
            closeTenantDetails()
        }
    }

    fun updateTenantStatus(tenant: Tenant, newStatus: TenantStatus) {
        viewModelScope.launch {
            repository.updateTenant(tenant.copy(status = newStatus))
            if (_selectedTenantForDetails.value?.id == tenant.id) {
                _selectedTenantForDetails.value = tenant.copy(status = newStatus)
            }
        }
    }

    fun updateTenantRent(tenant: Tenant, newRent: Double, targetIncrementYear: Int = 0) {
        viewModelScope.launch {
            val updated = tenant.copy(
                rent = newRent,
                lastIncrementYear = if (targetIncrementYear > 0) targetIncrementYear else tenant.lastIncrementYear
            )
            repository.updateTenant(updated)
            if (_selectedTenantForDetails.value?.id == tenant.id) {
                _selectedTenantForDetails.value = updated
            }
        }
    }

    private fun copyFileToInternalStorage(context: Context, uri: Uri): Pair<String, String>? {
        return try {
            val contentResolver = context.contentResolver
            var fileName = "agreement_${System.currentTimeMillis()}"

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }

            val agreementsDir = File(context.filesDir, "agreements")
            if (!agreementsDir.exists()) {
                agreementsDir.mkdirs()
            }

            val destinationFile = File(agreementsDir, "${System.currentTimeMillis()}_$fileName")
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Pair(Uri.fromFile(destinationFile).toString(), fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class TenantViewModelFactory(private val repository: TenantRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TenantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TenantViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
