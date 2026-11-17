package com.example.myapplication

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.AppRepository
import com.example.myapplication.location.LocationHelper
import com.example.myapplication.model.User
import com.example.myapplication.model.UserRole
import com.example.myapplication.ui.screens.AdminHomeScreen
import com.example.myapplication.ui.screens.EmployeeHomeScreen
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }
    val locationHelper = remember { LocationHelper(context) }

    var currentUser by remember { mutableStateOf(repository.getCurrentLoggedInUser()) }

    // Ruxsatlarni so'rash launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (!locationHelper.hasLocationPermission()) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val activeUser = currentUser
    if (activeUser == null) {
        LoginScreen(
            onLoginAttempt = { username, password ->
                repository.login(username, password)
            },
            onLoginSuccess = { user ->
                repository.setCurrentLoggedInUser(user)
                currentUser = user
            }
        )
    } else {
        when (activeUser.role) {
            UserRole.BOSHLIQ -> {
                AdminHomeScreen(
                    adminUser = activeUser,
                    repository = repository,
                    locationHelper = locationHelper,
                    onLogout = {
                        repository.setCurrentLoggedInUser(null)
                        currentUser = null
                    }
                )
            }
            UserRole.HODIM -> {
                EmployeeHomeScreen(
                    employeeUser = activeUser,
                    repository = repository,
                    locationHelper = locationHelper,
                    onLogout = {
                        repository.setCurrentLoggedInUser(null)
                        currentUser = null
                    }
                )
            }
        }
    }
}
