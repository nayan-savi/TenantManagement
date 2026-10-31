package com.example.tenantmanagement.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tenantmanagement.data.TenantDatabase
import com.example.tenantmanagement.data.TenantStatus
import com.example.tenantmanagement.utils.NotificationHelper
import kotlinx.coroutines.flow.first

class TenantReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = TenantDatabase.getDatabase(context)
            val activeTenants = database.tenantDao().getAllTenants().first().filter {
                it.status == TenantStatus.ACTIVE
            }

            activeTenants.forEach { tenant ->
                val anniversaryInfo = NotificationHelper.checkAnniversaryStatus(tenant)
                val forecastRent = NotificationHelper.calculateForecastRent(tenant.rent, tenant.yearlyIncrement)

                if (anniversaryInfo.shouldShowReminder) {
                    NotificationHelper.showAnniversaryNotification(
                        context = context,
                        tenant = tenant,
                        info = anniversaryInfo,
                        forecastRent = forecastRent
                    )
                } else if (anniversaryInfo.isOverdueOrDueToday && tenant.lastIncrementYear < anniversaryInfo.targetAnniversaryYear && forecastRent > tenant.rent) {
                    // Update rent in Database after anniversary date is reached / overdue
                    val oldRent = tenant.rent
                    val updatedTenant = tenant.copy(
                        rent = forecastRent,
                        lastIncrementYear = anniversaryInfo.targetAnniversaryYear
                    )
                    database.tenantDao().updateTenant(updatedTenant)

                    NotificationHelper.showRentUpdatedNotification(
                        context = context,
                        tenant = tenant,
                        oldRent = oldRent,
                        newRent = forecastRent
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
