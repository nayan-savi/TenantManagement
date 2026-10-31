package com.example.tenantmanagement.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.tenantmanagement.MainActivity
import com.example.tenantmanagement.R
import com.example.tenantmanagement.data.Tenant
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AnniversaryInfo(
    val isOneMonthBefore: Boolean,
    val isAnniversaryMonth: Boolean,
    val isOverdueOrDueToday: Boolean,
    val daysRemaining: Int,
    val nextAnniversaryDate: String = "",
    val tenancyYearNumber: Int = 1,
    val targetAnniversaryYear: Int = 0,
    val shouldShowReminder: Boolean = false,
    val hasBeenUpdatedThisYear: Boolean = false,
    val anniversaryMonthName: String = ""
)

object NotificationHelper {

    const val CHANNEL_ID = "tenant_anniversary_reminders"
    const val CHANNEL_NAME = "Tenant Rent & Renewal Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies 1 month before tenant annual joining anniversary and when rent is auto-updated."
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Calculates Forecast Rent = Current Rent + Yearly Increment (Percentage or Fixed Amount)
     */
    fun calculateForecastRent(currentRent: Double, incrementString: String): Double {
        val trimmed = incrementString.trim()
        if (trimmed.isBlank() || currentRent <= 0) return currentRent

        return try {
            if (trimmed.endsWith("%")) {
                val percentage = trimmed.removeSuffix("%").trim().toDoubleOrNull() ?: 0.0
                currentRent + (currentRent * percentage / 100.0)
            } else if (trimmed.startsWith("₹")) {
                val amount = trimmed.removePrefix("₹").trim().toDoubleOrNull() ?: 0.0
                currentRent + amount
            } else {
                val valDouble = trimmed.toDoubleOrNull() ?: 0.0
                if (valDouble in 1.0..50.0) {
                    currentRent + (currentRent * valDouble / 100.0)
                } else {
                    currentRent + valDouble
                }
            }
        } catch (e: Exception) {
            currentRent
        }
    }

    fun showAnniversaryNotification(context: Context, tenant: Tenant, info: AnniversaryInfo, forecastRent: Double) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            tenant.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currentRentInt = tenant.rent.toInt()
        val forecastRentInt = forecastRent.toInt()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Year ${info.tenancyYearNumber} Rent Renewal: ${tenant.name}")
            .setContentText("Current Rent: ₹$currentRentInt | Forecast Rent: ₹$forecastRentInt (+${tenant.yearlyIncrement}) for ${info.anniversaryMonthName} ${info.targetAnniversaryYear}.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Annual Year ${info.tenancyYearNumber} Renewal Reminder for ${tenant.name} (Flat #${tenant.flatNumber}).\n" +
                            "Joined Date: ${tenant.dateJoined}\n" +
                            "Upcoming Anniversary: ${info.anniversaryMonthName} ${info.targetAnniversaryYear} (${info.nextAnniversaryDate})\n" +
                            "Current Rent: ₹$currentRentInt\n" +
                            "Forecasted New Rent: ₹$forecastRentInt (Increment: ${tenant.yearlyIncrement})\n" +
                            "Tap to review or send renewal notice to tenant."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(tenant.id + 1000, notification)
    }

    fun showRentUpdatedNotification(context: Context, tenant: Tenant, oldRent: Double, newRent: Double) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            tenant.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Annual Rent Updated: ${tenant.name}")
            .setContentText("Anniversary reached for ${tenant.name}. Rent changed from ₹${oldRent.toInt()} to ₹${newRent.toInt()}.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Annual Anniversary Rent Update for ${tenant.name} (Flat #${tenant.flatNumber}).\n" +
                            "Joined Date: ${tenant.dateJoined}\n" +
                            "Previous Rent: ₹${oldRent.toInt()}\n" +
                            "New Rent (Updated): ₹${newRent.toInt()} (Yearly Increment: ${tenant.yearlyIncrement})\n" +
                            "This annual increment will repeat next year on the joining anniversary date."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(tenant.id + 2000, notification)
    }

    fun checkAnniversaryStatus(tenant: Tenant): AnniversaryInfo {
        return checkAnniversaryStatus(tenant.dateJoined, tenant.lastIncrementYear)
    }

    /**
     * Evaluates anniversary status by Month.
     * If current month is greater than joined month (e.g. June for May joiner), all notifications hide.
     */
    fun checkAnniversaryStatus(dateJoinedString: String, lastIncrementYear: Int = 0): AnniversaryInfo {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateJoined = sdf.parse(dateJoinedString) ?: return AnniversaryInfo(false, false, false, 0)

            val today = Calendar.getInstance()
            val joinedCalendar = Calendar.getInstance().apply { time = dateJoined }

            val joinedMonth = joinedCalendar.get(Calendar.MONTH) // 0-11
            val joinedYear = joinedCalendar.get(Calendar.YEAR)
            val todayMonth = today.get(Calendar.MONTH) // 0-11
            val todayYear = today.get(Calendar.YEAR)

            // Month prior to anniversary month (e.g. November for December)
            val oneMonthBeforeMonth = if (joinedMonth == 0) 11 else joinedMonth - 1

            // Determine target anniversary year
            val targetYear = if (todayMonth > joinedMonth) {
                todayYear + 1
            } else if (todayMonth == 0 && joinedMonth == 11) {
                todayYear
            } else {
                todayYear
            }

            // A tenant's 1st anniversary occurs strictly in targetYear > joinedYear!
            val isSameJoiningYear = (targetYear <= joinedYear)

            // If current month is greater than joined month (e.g. June for May joiner), anniversary for this year is over
            val isMonthAfterJoinedMonth = (todayMonth > joinedMonth) && (todayMonth != oneMonthBeforeMonth)

            val shouldHideAll = isSameJoiningYear || isMonthAfterJoinedMonth

            val isOneMonthBefore = !shouldHideAll && (todayMonth == oneMonthBeforeMonth)
            val isAnniversaryMonth = !shouldHideAll && (todayMonth == joinedMonth)

            val isPendingUpdateForThisYear = lastIncrementYear < targetYear
            val hasBeenUpdatedThisYear = lastIncrementYear >= targetYear

            // Show reminder 1 month before OR during anniversary month (ONLY if not hidden and not updated yet)
            val shouldShowReminder = !shouldHideAll && (isOneMonthBefore || (isAnniversaryMonth && isPendingUpdateForThisYear)) && isPendingUpdateForThisYear

            val tenancyYearNumber = (targetYear - joinedYear).coerceAtLeast(1)

            val nextAnniversaryCalendar = Calendar.getInstance().apply {
                time = dateJoined
                set(Calendar.YEAR, targetYear)
            }
            val nextAnniversaryDateString = sdf.format(nextAnniversaryCalendar.time)

            val diffInMillis = nextAnniversaryCalendar.timeInMillis - today.timeInMillis
            val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
            val daysRemaining = if (diffInDays > 0) diffInDays else 0

            val monthSdf = SimpleDateFormat("MMMM", Locale.getDefault())
            val anniversaryMonthName = monthSdf.format(nextAnniversaryCalendar.time)

            AnniversaryInfo(
                isOneMonthBefore = isOneMonthBefore,
                isAnniversaryMonth = isAnniversaryMonth,
                isOverdueOrDueToday = isAnniversaryMonth,
                daysRemaining = daysRemaining,
                nextAnniversaryDate = nextAnniversaryDateString,
                tenancyYearNumber = tenancyYearNumber,
                targetAnniversaryYear = targetYear,
                shouldShowReminder = shouldShowReminder,
                hasBeenUpdatedThisYear = hasBeenUpdatedThisYear && isAnniversaryMonth,
                anniversaryMonthName = anniversaryMonthName
            )
        } catch (e: Exception) {
            AnniversaryInfo(false, false, false, 0)
        }
    }
}
