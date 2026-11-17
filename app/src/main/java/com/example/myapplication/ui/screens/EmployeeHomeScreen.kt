package com.example.myapplication.ui.screens

import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AppRepository
import com.example.myapplication.location.LocationHelper
import com.example.myapplication.model.AttendanceRecord
import com.example.myapplication.model.Building
import com.example.myapplication.model.User
import com.example.myapplication.ui.components.LeafletMapView
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeHomeScreen(
    employeeUser: User,
    repository: AppRepository,
    locationHelper: LocationHelper,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // State
    var building by remember { mutableStateOf(repository.getBuilding()) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    var todayRecord by remember { mutableStateOf(repository.getTodayRecord(employeeUser.id)) }
    var myHistory by remember { mutableStateOf(repository.getUserHistory(employeeUser.id)) }

    // Distance and inside-geofence calculation
    val distanceToBuilding = remember(userLocation, building) {
        userLocation?.let { loc ->
            LocationHelper.calculateDistanceMeters(
                loc.latitude,
                loc.longitude,
                building.latitude,
                building.longitude
            )
        }
    }

    val isInsideBuilding = remember(distanceToBuilding, building) {
        distanceToBuilding != null && distanceToBuilding <= building.radiusInMeters
    }

    // Refresh location function
    fun refreshLocation() {
        isLocating = true
        building = repository.getBuilding()
        locationHelper.getCurrentLocation { loc ->
            isLocating = false
            if (loc != null) {
                userLocation = loc
            }
        }
    }

    // Har 15 soniyada lokatsiyani yangilab turish (Real-time monitoring)
    LaunchedEffect(Unit) {
        refreshLocation()
        while (true) {
            delay(15000)
            refreshLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = employeeUser.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${employeeUser.position.ifEmpty { "Hodim" }} • Smart Davomat",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiqish",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        todayRecord = repository.getTodayRecord(employeeUser.id)
                    },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text("Davomat", fontSize = 12.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        myHistory = repository.getUserHistory(employeeUser.id)
                    },
                    icon = { Text("📜", fontSize = 18.sp) },
                    label = { Text("Tarixim", fontSize = 12.sp) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> EmployeeAttendanceTab(
                    building = building,
                    userLocation = userLocation,
                    distanceToBuilding = distanceToBuilding,
                    isInsideBuilding = isInsideBuilding,
                    isLocating = isLocating,
                    todayRecord = todayRecord,
                    onRefreshLocation = { refreshLocation() },
                    onCheckIn = {
                        val loc = userLocation
                        if (loc != null) {
                            val rec = repository.checkIn(employeeUser, loc.latitude, loc.longitude)
                            todayRecord = rec
                            myHistory = repository.getUserHistory(employeeUser.id)
                            Toast.makeText(context, "Kirish qayd etildi! Vaqt: ${rec.checkInTime}", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Lokatsiya aniqlanmadi!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onCheckOut = {
                        val rec = repository.checkOut(employeeUser.id)
                        todayRecord = rec
                        myHistory = repository.getUserHistory(employeeUser.id)
                        Toast.makeText(context, "Chiqish qayd etildi! Vaqt: ${rec?.checkOutTime}", Toast.LENGTH_LONG).show()
                    },
                    onSimulateInside = {
                        val simulatedLoc = Location("simulated").apply {
                            latitude = building.latitude
                            longitude = building.longitude
                        }
                        userLocation = simulatedLoc
                        Toast.makeText(context, "Lokatsiya bino ichiga o'rnatildi (Simulyatsiya)", Toast.LENGTH_SHORT).show()
                    }
                )
                1 -> EmployeeHistoryTab(historyRecords = myHistory)
            }
        }
    }
}

@Composable
fun EmployeeAttendanceTab(
    building: Building,
    userLocation: Location?,
    distanceToBuilding: Float?,
    isInsideBuilding: Boolean,
    isLocating: Boolean,
    todayRecord: AttendanceRecord?,
    onRefreshLocation: () -> Unit,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onSimulateInside: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today Status Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (todayRecord != null) {
                    if (todayRecord.checkOutTime == null)
                        Color(0xFFE8F5E9)
                    else
                        Color(0xFFEDE7F6)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bugungi Davomat Holati",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = AppRepository.getTodayDateString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (todayRecord == null) {
                    Text(
                        text = "Bugun hali davomatdan o'tmadingiz.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Kirish vaqti", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = todayRecord.checkInTime ?: "--",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Chiqish vaqti", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = todayRecord.checkOutTime ?: "Ishda",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (todayRecord.checkOutTime != null) Color(0xFF512DA8) else Color(0xFFF57C00)
                            )
                        }
                    }
                }
            }
        }

        // Real-Time Location & Geofence Indicator Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isInsideBuilding)
                    Color(0xFFE8F5E9)
                else
                    Color(0xFFFFEBEE)
            ),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            color = if (isInsideBuilding) Color(0xFF4CAF50) else Color(0xFFE53935),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isInsideBuilding) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isInsideBuilding)
                        "SIZ BINO HUDUDIDASIZ!"
                    else
                        "SIZ BINO HUDUDIDA EMASSIZ!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (isInsideBuilding) Color(0xFF2E7D32) else Color(0xFFC62828),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (distanceToBuilding != null) {
                    Text(
                        text = "Binogacha masofa: ${distanceToBuilding.toInt()} metr",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                } else {
                    Text(
                        text = "Lokatsiya aniqlanmoqda...",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "Bino: ${building.name} (Ruxsat etilgan radius: ${building.radiusInMeters.toInt()}m)",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: Check-in / Check-out
                if (todayRecord == null || todayRecord.checkInTime == null) {
                    Button(
                        onClick = onCheckIn,
                        enabled = isInsideBuilding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (isInsideBuilding) "🏢 Kirishni qayd etish (Check-In)" else "Bino ichiga kiring",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (todayRecord.checkOutTime == null) {
                    Button(
                        onClick = onCheckOut,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "🚪 Chiqishni qayd etish (Check-Out)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Bugungi kunlik davomat yakunlangan ✅",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Lokatsiyani yangilash & Simulyatsiya tugmalari
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onRefreshLocation,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLocating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text("GPS Yangilash", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onSimulateInside,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Binoga kirish (Test)", fontSize = 12.sp)
            }
        }

        // Xarita: Bino va Hodim joylashuvi
        Text(
            text = "Xaritada joylashuv",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        LeafletMapView(
            latitude = building.latitude,
            longitude = building.longitude,
            radiusMeters = building.radiusInMeters,
            userLatitude = userLocation?.latitude,
            userLongitude = userLocation?.longitude,
            isSelectable = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun EmployeeHistoryTab(historyRecords: List<AttendanceRecord>) {
    if (historyRecords.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Hozircha davomat tarixingiz mavjud emas.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Mening Davomat Tarixim",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            items(historyRecords) { rec ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = rec.date,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = rec.status,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rec.status == "Ishda") Color(0xFF2E7D32) else Color.DarkGray
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kirgan vaqt: ${rec.checkInTime ?: "--"}",
                                fontSize = 13.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Chiqgan vaqt: ${rec.checkOutTime ?: "Hozirda ishda"}",
                                fontSize = 13.sp,
                                color = if (rec.checkOutTime != null) Color(0xFF512DA8) else Color(0xFFF57C00),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
