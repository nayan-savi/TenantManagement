package com.example.tenantmanagement

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tenantmanagement.data.TenantDatabase
import com.example.tenantmanagement.data.TenantRepository
import com.example.tenantmanagement.ui.DashboardScreen
import com.example.tenantmanagement.ui.TenantViewModel
import com.example.tenantmanagement.ui.TenantViewModelFactory
import com.example.tenantmanagement.ui.theme.TenantManagementTheme
import com.example.tenantmanagement.workers.TenantReminderWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = TenantDatabase.getDatabase(applicationContext)
        val repository = TenantRepository(database.tenantDao(), database.flatDao(), database.buildingDao())
        val viewModelFactory = TenantViewModelFactory(repository)

        // Enqueue immediate one-time check and periodic background check for reminders
        val workManager = WorkManager.getInstance(applicationContext)
        val immediateWorkRequest = OneTimeWorkRequestBuilder<TenantReminderWorker>().build()
        workManager.enqueue(immediateWorkRequest)

        val reminderWorkRequest = PeriodicWorkRequestBuilder<TenantReminderWorker>(24, TimeUnit.HOURS).build()
        workManager.enqueueUniquePeriodicWork(
            "TenantAnniversaryReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWorkRequest
        )

        setContent {
            val viewModel: TenantViewModel = viewModel(factory = viewModelFactory)

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    workManager.enqueue(immediateWorkRequest)
                }
            }

            LaunchedEffect(Unit) {
                viewModel.initThemeMode(applicationContext)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            val themeMode by viewModel.themeMode.collectAsState()

            TenantManagementTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
