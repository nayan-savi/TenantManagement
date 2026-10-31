package com.example.tenantmanagement.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tenantmanagement.data.Building
import com.example.tenantmanagement.data.Tenant
import com.example.tenantmanagement.data.TenantStatus
import com.example.tenantmanagement.utils.NotificationHelper

@Composable
fun TenantDetailDialog(
    tenant: Tenant,
    buildings: List<Building> = emptyList(),
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (TenantStatus) -> Unit,
    onUpdateRentToForecast: (newRent: Double, targetYear: Int) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRentConfirmDialog by remember { mutableStateOf(false) }
    var hasUpdatedRentInDialog by remember { mutableStateOf(false) }

    val matchingBuilding = buildings.find { it.name.equals(tenant.buildingName, ignoreCase = true) }
    val ownerName = matchingBuilding?.ownerName?.trim()?.ifBlank { null } ?: "Property Owner"

    val anniversaryInfo = NotificationHelper.checkAnniversaryStatus(tenant)
    val forecastRent = NotificationHelper.calculateForecastRent(tenant.rent, tenant.yearlyIncrement)
    val isUpdatedThisCycle = anniversaryInfo.hasBeenUpdatedThisYear || hasUpdatedRentInDialog
    val shouldShowReminder = anniversaryInfo.shouldShowReminder && !hasUpdatedRentInDialog
    val canUpdateRent = anniversaryInfo.isAnniversaryMonth && shouldShowReminder

    var isWhatsAppSentState by remember(tenant.id, anniversaryInfo.targetAnniversaryYear) {
        mutableStateOf(isWhatsAppNoticeSent(context, tenant.id, anniversaryInfo.targetAnniversaryYear))
    }

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
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
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tenant.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Flat #${tenant.flatNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = statusBgColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = tenant.status.displayName,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HorizontalDivider()

                // Upcoming Anniversary Banner (Shown 1 Month Before or in Anniversary Month before update)
                if (shouldShowReminder) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFFED6C02)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Year ${anniversaryInfo.tenancyYearNumber} Annual Renewal Notice",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                                Text(
                                    text = if (anniversaryInfo.isAnniversaryMonth)
                                        "Anniversary Due This Month (${anniversaryInfo.nextAnniversaryDate})! Tap button below to update rent."
                                    else
                                        "Upcoming Anniversary: ${anniversaryInfo.nextAnniversaryDate} (~${anniversaryInfo.daysRemaining} days remaining).\n" +
                                                "Current Rent: ₹${tenant.rent.toInt()} ➔ Forecast Rent: ₹${forecastRent.toInt()} (+${tenant.yearlyIncrement})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }

                // Updated Success Notice Banner (Shows after rent update during the anniversary month)
                if (isUpdatedThisCycle && anniversaryInfo.isAnniversaryMonth) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "✓ Rent Updated for Year ${anniversaryInfo.tenancyYearNumber}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "Rent is updated to ₹${tenant.rent.toInt()} for ${anniversaryInfo.anniversaryMonthName} ${anniversaryInfo.targetAnniversaryYear}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                // Financial Overview Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (shouldShowReminder) "Current Rent" else "Monthly Rent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${tenant.rent.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (shouldShowReminder) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Forecast Rent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF1B5E20)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${forecastRent.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Advance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${tenant.advanceAmount.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                // Anniversary Month Actions (Update Rent & Send WhatsApp Notice next to it)
                val isPhonePresent = tenant.phoneNumber.isNotBlank()

                if (anniversaryInfo.isAnniversaryMonth) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Update Rent Button (shown before rent is updated for this cycle)
                            if (shouldShowReminder) {
                                Button(
                                    onClick = { showRentConfirmDialog = true },
                                    enabled = canUpdateRent,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.height(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Update Rent (₹${forecastRent.toInt()})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            // 2. WhatsApp Notice Button (next to Forecast / Update button)
                            if (isWhatsAppSentState) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            openWhatsAppNotice(context, tenant, forecastRent, ownerName)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF25D366),
                                            modifier = Modifier.height(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "✓ Notice Sent",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1B5E20)
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        isWhatsAppSentState = true
                                        markWhatsAppNoticeSent(context, tenant.id, anniversaryInfo.targetAnniversaryYear)
                                        openWhatsAppNotice(context, tenant, forecastRent, ownerName)
                                    },
                                    enabled = isPhonePresent,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF25D366),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0xFF25D366).copy(alpha = 0.35f),
                                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "WhatsApp",
                                        tint = Color.White,
                                        modifier = Modifier.height(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "WhatsApp Notice",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        if (!isPhonePresent && !isWhatsAppSentState) {
                            Text(
                                text = "⚠️ Add tenant phone number to enable WhatsApp messaging",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else if (shouldShowReminder) {
                    // Shown 1 month before anniversary month
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { showRentConfirmDialog = true },
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Update Rent to Forecasted ₹${forecastRent.toInt()}")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Button enables in ${anniversaryInfo.anniversaryMonthName} (Anniversary Month)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (tenant.emailAddress.isNotBlank() && (shouldShowReminder || (isUpdatedThisCycle && anniversaryInfo.isAnniversaryMonth))) {
                    OutlinedButton(
                        onClick = { sendEmailNotice(context, tenant, forecastRent, ownerName) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.height(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Email Notice to Tenant")
                    }
                }

                if (tenant.notes.isNotBlank()) {
                    DetailRow(icon = Icons.Default.Home, label = "Notes", value = tenant.notes)
                }

                // Rental Agreement Section
                Text(
                    text = "Agreement Document",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (tenant.agreementUri != null)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
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
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tenant.agreementFileName ?: "No Agreement Attached",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (tenant.agreementUri != null) "Tap to open document" else "You can attach an agreement by editing",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (tenant.agreementUri != null) {
                            IconButton(onClick = {
                                try {
                                    val uri = Uri.parse(tenant.agreementUri)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open agreement file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "View Agreement")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                Button(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    if (showRentConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRentConfirmDialog = false },
            title = { Text("Confirm Rent Update") },
            text = {
                Text(
                    "Are you sure you want to update monthly rent for ${tenant.name} from ₹${tenant.rent.toInt()} to ₹${forecastRent.toInt()} (+${tenant.yearlyIncrement})?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRentConfirmDialog = false
                        hasUpdatedRentInDialog = true
                        onUpdateRentToForecast(forecastRent, anniversaryInfo.targetAnniversaryYear)
                        Toast.makeText(context, "Rent successfully updated to ₹${forecastRent.toInt()}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Update Rent")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRentConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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

fun isWhatsAppNoticeSent(context: Context, tenantId: Int, targetYear: Int): Boolean {
    val prefs = context.getSharedPreferences("tenant_mgmt_prefs", Context.MODE_PRIVATE)
    return prefs.getBoolean("whatsapp_sent_${tenantId}_$targetYear", false)
}

fun markWhatsAppNoticeSent(context: Context, tenantId: Int, targetYear: Int) {
    val prefs = context.getSharedPreferences("tenant_mgmt_prefs", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("whatsapp_sent_${tenantId}_$targetYear", true).apply()
}

fun sendEmailNotice(context: Context, tenant: Tenant, forecastRent: Double, ownerName: String = "Property Owner") {
    val anniversaryInfo = NotificationHelper.checkAnniversaryStatus(tenant)
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${tenant.emailAddress}")
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Year ${anniversaryInfo.tenancyYearNumber} Annual Rent Renewal Notice - Flat #${tenant.flatNumber}"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "Dear ${tenant.name},\n\n" +
                        "This is a friendly reminder regarding your Year ${anniversaryInfo.tenancyYearNumber} tenancy at Flat #${tenant.flatNumber}${if (tenant.buildingName.isNotBlank()) ", ${tenant.buildingName}" else ""}.\n\n" +
                        "• Joined Date: ${tenant.dateJoined}\n" +
                        "• Upcoming Annual Anniversary: ${anniversaryInfo.nextAnniversaryDate}\n" +
                        "• Current Monthly Rent: ₹${tenant.rent.toInt()}\n" +
                        "• Yearly Increment Rate: ${tenant.yearlyIncrement}\n" +
                        "• Forecasted New Rent: ₹${forecastRent.toInt()}\n\n" +
                        "Please note that upon your upcoming anniversary (${anniversaryInfo.nextAnniversaryDate}), the monthly rent will update to ₹${forecastRent.toInt()}.\n\n" +
                        "Please let us know if you have any questions.\n\n" +
                        "Best regards,\n" +
                        ownerName
            )
        }
        context.startActivity(Intent.createChooser(intent, "Send Email Notice"))
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open email app: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

fun openWhatsAppNotice(context: Context, tenant: Tenant, forecastRent: Double, ownerName: String = "Property Owner") {
    val anniversaryInfo = NotificationHelper.checkAnniversaryStatus(tenant)

    var phone = tenant.phoneNumber.replace("[^0-9]".toRegex(), "")
    if (phone.length == 10) {
        phone = "91$phone"
    }

    val currentRentInt = tenant.rent.toInt()
    val forecastRentInt = forecastRent.toInt()

    val effectiveDateFromFirstOfMonth = try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        if (anniversaryInfo.nextAnniversaryDate.isNotBlank()) {
            val parsedDate = sdf.parse(anniversaryInfo.nextAnniversaryDate)
            if (parsedDate != null) {
                cal.time = parsedDate
            }
        }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        sdf.format(cal.time)
    } catch (e: Exception) {
        anniversaryInfo.nextAnniversaryDate
    }

    val message = """
        *RENT REVISION & BREAKDOWN NOTICE*

        Dear *${tenant.name}*,

        Hope you are doing well.

        This is a formal notice regarding the annual rent revision for your tenancy at *Flat #${tenant.flatNumber}*${if (tenant.buildingName.isNotBlank()) " (${tenant.buildingName})" else ""}.

        *Tenancy & Rent Breakdown Details:*
        • *Tenant Name:* ${tenant.name}
        • *Property / Flat:* Flat #${tenant.flatNumber}${if (tenant.buildingName.isNotBlank()) ", ${tenant.buildingName}" else ""}
        • *Joined Date:* ${tenant.dateJoined}
        • *Tenancy Cycle:* Year ${anniversaryInfo.tenancyYearNumber}
        • *Previous Rent:* ₹$currentRentInt / month
        • *Yearly Increment:* ${tenant.yearlyIncrement.ifBlank { "5%" }}
        • *New Updated Rent:* ₹$forecastRentInt / month
        • *Effective Date:* $effectiveDateFromFirstOfMonth

        Please note that your monthly rent is updated to *₹$forecastRentInt / month* starting from *$effectiveDateFromFirstOfMonth*.

        Please let us know if you have any questions or require further clarification.

        Thank you for your cooperation!

        Best regards,
        *$ownerName*
    """.trimIndent()

    try {
        val encodedMessage = Uri.encode(message)
        val url = if (phone.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$phone&text=$encodedMessage"
        } else {
            "https://api.whatsapp.com/send?text=$encodedMessage"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.whatsapp")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val encodedMessage = Uri.encode(message)
            val fallbackUrl = if (phone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$phone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
            context.startActivity(fallbackIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, "WhatsApp is not installed on this device.", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
