package com.example.tenantmanagement.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tenantmanagement.data.Building
import com.example.tenantmanagement.data.Flat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFlatDialog(
    flat: Flat?,
    buildings: List<Building> = emptyList(),
    preselectedBuildingName: String? = null,
    onDismiss: () -> Unit,
    onSave: (
        flatId: Int,
        buildingId: Int,
        buildingName: String,
        locality: String,
        flatNumber: String,
        ecAccountNumber: String,
        rrNumber: String,
        waterMeterNumber: String,
        notes: String
    ) -> Unit
) {
    val initialBuilding = buildings.find {
        it.name.equals(preselectedBuildingName ?: flat?.buildingName ?: "", ignoreCase = true) ||
                (flat?.buildingId != null && flat.buildingId > 0 && it.id == flat.buildingId)
    }

    var buildingName by remember { mutableStateOf(preselectedBuildingName ?: flat?.buildingName ?: "") }
    var selectedBuildingId by remember { mutableStateOf(flat?.buildingId ?: initialBuilding?.id ?: 0) }
    var locality by remember {
        mutableStateOf(
            if (flat != null && flat.locality.isNotBlank()) flat.locality
            else initialBuilding?.locality ?: ""
        )
    }
    var flatNumber by remember { mutableStateOf(flat?.flatNumber ?: "") }
    var ecAccountNumber by remember { mutableStateOf(flat?.ecAccountNumber ?: "") }
    var rrNumber by remember { mutableStateOf(flat?.rrNumber ?: "") }
    var waterMeterNumber by remember { mutableStateOf(flat?.waterMeterNumber ?: "") }
    var notes by remember { mutableStateOf(flat?.notes ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (flat == null) "Add Property / Flat" else "Edit Property / Flat",
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
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Building Selection Dropdown or Free Input
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded && buildings.isNotEmpty(),
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = buildingName,
                        onValueChange = {
                            buildingName = it
                            errorMessage = null
                        },
                        label = { Text("Building / Property Name *") },
                        placeholder = { Text(if (buildings.isEmpty()) "e.g. Sunrise Heights" else "Select or type building name") },
                        leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null) },
                        trailingIcon = {
                            if (buildings.isNotEmpty()) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    if (buildings.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            buildings.forEach { buildingItem ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(text = buildingItem.name, fontWeight = FontWeight.Bold)
                                            if (buildingItem.locality.isNotBlank()) {
                                                Text(
                                                    text = buildingItem.locality,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        buildingName = buildingItem.name
                                        selectedBuildingId = buildingItem.id
                                        if (buildingItem.locality.isNotBlank() && locality.isBlank()) {
                                            locality = buildingItem.locality
                                        }
                                        dropdownExpanded = false
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = locality,
                    onValueChange = { locality = it },
                    label = { Text("Locality / Area") },
                    placeholder = { Text("e.g. Indiranagar, Stage 2") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = flatNumber,
                    onValueChange = { flatNumber = it; errorMessage = null },
                    label = { Text("Flat / House Number *") },
                    placeholder = { Text("e.g. Flat 101 or House 22A") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ecAccountNumber,
                    onValueChange = { ecAccountNumber = it },
                    label = { Text("EC Account Number") },
                    placeholder = { Text("Electricity Account #") },
                    leadingIcon = { Icon(Icons.Default.ElectricMeter, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rrNumber,
                    onValueChange = { rrNumber = it },
                    label = { Text("RR Number") },
                    placeholder = { Text("e.g. N2-4412") },
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = waterMeterNumber,
                    onValueChange = { waterMeterNumber = it },
                    label = { Text("Water Meter Number") },
                    placeholder = { Text("e.g. WM-8820") },
                    leadingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Description") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (buildingName.isBlank()) {
                        errorMessage = "Please enter or select Building Name"
                        return@Button
                    }
                    if (flatNumber.isBlank()) {
                        errorMessage = "Please enter Flat Number"
                        return@Button
                    }

                    showConfirmationDialog = true
                }
            ) {
                Text(if (flat == null) "Add Property" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text(if (flat == null) "Confirm Add Property" else "Confirm Save Changes") },
            text = {
                Text(
                    if (flat == null)
                        "Are you sure you want to add Flat '${flatNumber.trim()}' in ${buildingName.trim()}?"
                    else
                        "Are you sure you want to save changes for Flat '${flatNumber.trim()}' in ${buildingName.trim()}?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        onSave(
                            flat?.id ?: 0,
                            selectedBuildingId,
                            buildingName.trim(),
                            locality.trim(),
                            flatNumber.trim(),
                            ecAccountNumber.trim(),
                            rrNumber.trim(),
                            waterMeterNumber.trim(),
                            notes.trim()
                        )
                    }
                ) {
                    Text("Confirm & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
