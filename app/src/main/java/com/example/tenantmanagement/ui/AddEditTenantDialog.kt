package com.example.tenantmanagement.ui

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.tenantmanagement.data.Building
import com.example.tenantmanagement.data.Flat
import com.example.tenantmanagement.data.Tenant
import com.example.tenantmanagement.data.TenantStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AadharVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val out = StringBuilder()
        for (i in raw.indices) {
            out.append(raw[i])
            if (i == 3 || i == 7) {
                out.append(' ')
            }
        }

        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                if (offset <= 12) return offset + 2
                return offset + 2
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                return 12
            }
        }

        return TransformedText(AnnotatedString(out.toString()), numberOffsetTranslator)
    }
}

fun formatAadharNumber(input: String): String {
    val digits = input.filter { it.isDigit() }.take(12)
    val builder = StringBuilder()
    for (i in digits.indices) {
        if (i > 0 && i % 4 == 0) {
            builder.append(' ')
        }
        builder.append(digits[i])
    }
    return builder.toString()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTenantDialog(
    tenant: Tenant?,
    buildings: List<Building> = emptyList(),
    flats: List<Flat> = emptyList(),
    tenants: List<Tenant> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        tenantId: Int,
        name: String,
        flatNumber: String,
        buildingName: String,
        flatId: Int?,
        dateJoined: String,
        rent: Double,
        advanceAmount: Double,
        aadharNumber: String,
        status: TenantStatus,
        yearlyIncrement: String,
        phoneNumber: String,
        emailAddress: String,
        notes: String,
        agreementUri: Uri?,
        existingAgreementUriString: String?,
        existingAgreementFileName: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(tenant?.name ?: "") }
    var buildingName by remember { mutableStateOf(tenant?.buildingName ?: "") }
    var flatNumber by remember { mutableStateOf(tenant?.flatNumber ?: "") }
    var selectedFlatId by remember { mutableStateOf(tenant?.flatId) }

    var dateJoined by remember { mutableStateOf(tenant?.dateJoined ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var rentText by remember { mutableStateOf(tenant?.rent?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var advanceText by remember { mutableStateOf(tenant?.advanceAmount?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var aadharNumber by remember { mutableStateOf(tenant?.aadharNumber?.filter { it.isDigit() }?.take(12) ?: "") }
    var status by remember { mutableStateOf(tenant?.status ?: TenantStatus.ACTIVE) }
    var yearlyIncrement by remember { mutableStateOf(tenant?.yearlyIncrement ?: "5%") }
    var phoneNumber by remember { mutableStateOf(tenant?.phoneNumber ?: "") }
    var emailAddress by remember { mutableStateOf(tenant?.emailAddress ?: "") }
    var notes by remember { mutableStateOf(tenant?.notes ?: "") }

    var selectedAgreementUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAgreementName by remember { mutableStateOf(tenant?.agreementFileName) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var buildingDropdownExpanded by remember { mutableStateOf(false) }
    var flatDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    val selectedBuilding = buildings.find { it.name.equals(buildingName, ignoreCase = true) }

    // Filter flats based on selected building
    val buildingFlats = if (buildingName.isBlank()) {
        flats
    } else {
        flats.filter {
            it.buildingName.equals(buildingName, ignoreCase = true) ||
                    (selectedBuilding != null && selectedBuilding.id > 0 && it.buildingId == selectedBuilding.id)
        }
    }

    // Filter out occupied flats (flats with active tenants other than current tenant being edited)
    val occupiedFlatNumbers = tenants
        .filter { it.status == TenantStatus.ACTIVE && it.id != tenant?.id }
        .map { it.flatNumber.lowercase().trim() }
        .toSet()

    val vacantFlats = buildingFlats.filter { flat ->
        !occupiedFlatNumbers.contains(flat.flatNumber.lowercase().trim())
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAgreementUri = uri
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    selectedAgreementName = cursor.getString(nameIndex)
                } else {
                    selectedAgreementName = "Agreement_Document"
                }
            } ?: run {
                selectedAgreementName = "Agreement_Document"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (tenant == null) "Add New Tenant" else "Edit Tenant Details",
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

                // Field 1: Tenant Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Tenant Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Field 2: Select Building
                if (buildings.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = buildingDropdownExpanded,
                        onExpandedChange = { buildingDropdownExpanded = !buildingDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = buildingName,
                            onValueChange = { buildingName = it },
                            label = { Text("Select Building *") },
                            placeholder = { Text("Select building") },
                            leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buildingDropdownExpanded) },
                            readOnly = true,
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = buildingDropdownExpanded,
                            onDismissRequest = { buildingDropdownExpanded = false }
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
                                        flatNumber = ""
                                        selectedFlatId = null
                                        buildingDropdownExpanded = false
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = buildingName,
                        onValueChange = { buildingName = it },
                        label = { Text("Building Name") },
                        placeholder = { Text("e.g. Sunrise Heights") },
                        leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                val isBuildingSelected = buildingName.isNotBlank() || buildings.isEmpty()

                // Field 3: Select Flat / House Number
                ExposedDropdownMenuBox(
                    expanded = flatDropdownExpanded && isBuildingSelected && vacantFlats.isNotEmpty(),
                    onExpandedChange = {
                        if (isBuildingSelected) {
                            flatDropdownExpanded = !flatDropdownExpanded
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = flatNumber,
                        onValueChange = {
                            flatNumber = it
                            errorMessage = null
                        },
                        enabled = isBuildingSelected,
                        label = { Text("Select Flat / House *") },
                        placeholder = {
                            Text(
                                if (!isBuildingSelected) "Please select a building first"
                                else if (vacantFlats.isEmpty() && buildingFlats.isNotEmpty()) "All flats in this building are occupied"
                                else if (buildingFlats.isEmpty()) "Type Flat Number"
                                else "Select from available vacant flats"
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        trailingIcon = {
                            if (isBuildingSelected && vacantFlats.isNotEmpty()) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = flatDropdownExpanded)
                            }
                        },
                        readOnly = vacantFlats.isNotEmpty(),
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    if (isBuildingSelected && vacantFlats.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = flatDropdownExpanded,
                            onDismissRequest = { flatDropdownExpanded = false }
                        ) {
                            vacantFlats.forEach { flatItem ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Flat ${flatItem.flatNumber} (Vacant)",
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    onClick = {
                                        flatNumber = flatItem.flatNumber
                                        if (buildingName.isBlank()) {
                                            buildingName = flatItem.buildingName
                                        }
                                        selectedFlatId = flatItem.id
                                        flatDropdownExpanded = false
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                }

                // Field 4: Joining Date
                OutlinedTextField(
                    value = dateJoined,
                    onValueChange = { dateJoined = it },
                    label = { Text("Joining Date *") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Field 5: Rent Amount
                OutlinedTextField(
                    value = rentText,
                    onValueChange = { rentText = it; errorMessage = null },
                    label = { Text("Rent (₹) *") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Field 6: Advance Amount
                OutlinedTextField(
                    value = advanceText,
                    onValueChange = { advanceText = it },
                    label = { Text("Advance Deposit (₹)") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Field 7: Aadhar Number
                OutlinedTextField(
                    value = aadharNumber,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 12) {
                            aadharNumber = digits
                        }
                    },
                    label = { Text("Aadhar Number") },
                    placeholder = { Text("1234 5678 9012") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = AadharVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Field 8: Yearly Increment
                OutlinedTextField(
                    value = yearlyIncrement,
                    onValueChange = { yearlyIncrement = it },
                    label = { Text("Yearly Increment") },
                    placeholder = { Text("e.g. 10% or ₹2000") },
                    leadingIcon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Field 9: Phone Number
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Field 10: Email Address
                OutlinedTextField(
                    value = emailAddress,
                    onValueChange = { emailAddress = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("tenant@example.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Field 10: Tenant Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = status.displayName,
                        onValueChange = {},
                        label = { Text("Tenant Status *") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false }
                    ) {
                        TenantStatus.entries.forEach { statusOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = statusOption.displayName,
                                        fontWeight = if (status == statusOption) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    status = statusOption
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Field 11: Agreement Attachment
                Text(
                    text = "Rental Agreement Attachment",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedAgreementName ?: "No agreement attached",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedAgreementName != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }

                        if (selectedAgreementName != null) {
                            IconButton(onClick = {
                                selectedAgreementUri = null
                                selectedAgreementName = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove file")
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    filePickerLauncher.launch(arrayOf("application/pdf", "image/*", "*/*"))
                                }
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Attach")
                            }
                        }
                    }
                }

                // Field 12: Notes / Additional Details
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Additional Details") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter Tenant Name"
                        return@Button
                    }
                    if (flatNumber.isBlank()) {
                        errorMessage = "Please select or enter Flat Number"
                        return@Button
                    }
                    val rentVal = rentText.toDoubleOrNull()
                    if (rentVal == null || rentVal <= 0) {
                        errorMessage = "Please enter a valid Rent amount"
                        return@Button
                    }

                    showConfirmationDialog = true
                }
            ) {
                Text(if (tenant == null) "Add Tenant" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showConfirmationDialog) {
        val rentVal = rentText.toDoubleOrNull() ?: 0.0
        val advanceVal = advanceText.toDoubleOrNull() ?: 0.0

        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text(if (tenant == null) "Confirm Add Tenant" else "Confirm Save Changes") },
            text = {
                Text(
                    if (tenant == null)
                        "Are you sure you want to add tenant '${name.trim()}' for Flat '${flatNumber.trim()}'?"
                    else
                        "Are you sure you want to save changes for tenant '${name.trim()}'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        onSave(
                            tenant?.id ?: 0,
                            name.trim(),
                            flatNumber.trim(),
                            buildingName.trim(),
                            selectedFlatId,
                            dateJoined.trim(),
                            rentVal,
                            advanceVal,
                            formatAadharNumber(aadharNumber),
                            status,
                            yearlyIncrement.trim(),
                            phoneNumber.trim(),
                            emailAddress.trim(),
                            notes.trim(),
                            selectedAgreementUri,
                            if (selectedAgreementUri == null) tenant?.agreementUri else null,
                            if (selectedAgreementUri == null) tenant?.agreementFileName else null
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            dateJoined = sdf.format(Date(selectedMillis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
