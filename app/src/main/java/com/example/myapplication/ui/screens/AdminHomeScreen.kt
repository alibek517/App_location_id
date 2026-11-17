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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AppRepository
import com.example.myapplication.location.LocationHelper
import com.example.myapplication.model.AttendanceRecord
import com.example.myapplication.model.Building
import com.example.myapplication.model.User
import com.example.myapplication.model.UserRole
import com.example.myapplication.ui.components.LeafletMapView
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    adminUser: User,
    repository: AppRepository,
    locationHelper: LocationHelper,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // State
    var building by remember { mutableStateOf(repository.getBuilding()) }
    var employees by remember { mutableStateOf(repository.getUsers().filter { it.role == UserRole.HODIM }) }
    var todayRecords by remember { mutableStateOf(repository.getTodayAttendance()) }
    var historyRecords by remember { mutableStateOf(repository.getAllAttendance()) }

    // Add employee dialog
    var showAddEmployeeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Boshliq Paneli",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = adminUser.name,
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
                        todayRecords = repository.getTodayAttendance()
                        employees = repository.getUsers().filter { it.role == UserRole.HODIM }
                    },
                    icon = { Text("📊", fontSize = 18.sp) },
                    label = { Text("Davomat", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        building = repository.getBuilding()
                    },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text("Bino & Xarita", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        employees = repository.getUsers().filter { it.role == UserRole.HODIM }
                    },
                    icon = { Text("👥", fontSize = 18.sp) },
                    label = { Text("Hodimlar", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        historyRecords = repository.getAllAttendance()
                    },
                    icon = { Text("📜", fontSize = 18.sp) },
                    label = { Text("Tarix", fontSize = 11.sp) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 2) {
                FloatingActionButton(
                    onClick = { showAddEmployeeDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Hodim qo'shish")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> AdminTodayAttendanceTab(
                    building = building,
                    employees = employees,
                    todayRecords = todayRecords
                )
                1 -> AdminBuildingLocationTab(
                    initialBuilding = building,
                    locationHelper = locationHelper,
                    onSaveBuilding = { updated ->
                        repository.saveBuilding(updated)
                        building = updated
                        Toast.makeText(context, "Bino ma'lumotlari saqlandi!", Toast.LENGTH_SHORT).show()
                    }
                )
                2 -> AdminEmployeesTab(
                    employees = employees,
                    onDelete = { empId ->
                        repository.deleteUser(empId)
                        employees = repository.getUsers().filter { it.role == UserRole.HODIM }
                        Toast.makeText(context, "Hodim o'chirildi", Toast.LENGTH_SHORT).show()
                    }
                )
                3 -> AdminHistoryTab(historyRecords = historyRecords)
            }
        }
    }

    if (showAddEmployeeDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddEmployeeDialog = false },
            onAdd = { newEmp ->
                val success = repository.addUser(newEmp)
                if (success) {
                    employees = repository.getUsers().filter { it.role == UserRole.HODIM }
                    showAddEmployeeDialog = false
                    Toast.makeText(context, "Yangi hodim muvaffaqiyatli qo'shildi!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Bunday loginli hodim allaqachon mavjud!", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

// 1. TODAY ATTENDANCE TAB
@Composable
fun AdminTodayAttendanceTab(
    building: Building,
    employees: List<User>,
    todayRecords: List<AttendanceRecord>
) {
    val totalEmployees = employees.size
    val presentCount = todayRecords.size
    val activeInBuilding = todayRecords.count { it.checkOutTime == null }
    val absentCount = (totalEmployees - presentCount).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Stats Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bugungi Davomat Statistikasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Sana: ${AppRepository.getTodayDateString()} | Bino: ${building.name}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(title = "Jami", value = totalEmployees.toString(), color = MaterialTheme.colorScheme.primary)
                        StatItem(title = "Kelganlar", value = presentCount.toString(), color = MaterialTheme.colorScheme.tertiary)
                        StatItem(title = "Binoda", value = activeInBuilding.toString(), color = MaterialTheme.colorScheme.secondary)
                        StatItem(title = "Kelmagan", value = absentCount.toString(), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        item {
            Text(
                text = "Hodimlar holati (Bugun)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (employees.isEmpty()) {
            item {
                Text(
                    text = "Hodimlar mavjud emas. 'Hodimlar' bo'limidan qo'shing.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(employees) { employee ->
                val record = todayRecords.firstOrNull { it.userId == employee.id }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (record != null)
                            MaterialTheme.colorScheme.surface
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = if (record != null) {
                                        if (record.checkOutTime == null)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.secondaryContainer
                                    } else MaterialTheme.colorScheme.errorContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (record != null) Icons.Default.Check else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (record != null) {
                                    if (record.checkOutTime == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                } else MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = employee.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = employee.position.ifEmpty { "Hodim" },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (record != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Kirish: ${record.checkInTime ?: "--"}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Chiqish: ${record.checkOutTime ?: "Hozir binoda"}",
                                        fontSize = 11.sp,
                                        color = if (record.checkOutTime != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Status badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (record == null)
                                        MaterialTheme.colorScheme.errorContainer
                                    else if (record.checkOutTime == null)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (record == null) "Kelmagan"
                                else if (record.checkOutTime == null) "Binoda"
                                else "Ketgan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (record == null)
                                    MaterialTheme.colorScheme.onErrorContainer
                                else if (record.checkOutTime == null)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(title: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

// 2. BUILDING LOCATION & GEOFENCE TAB
@Composable
fun AdminBuildingLocationTab(
    initialBuilding: Building,
    locationHelper: LocationHelper,
    onSaveBuilding: (Building) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialBuilding.name) }
    var lat by remember { mutableStateOf(initialBuilding.latitude.toString()) }
    var lng by remember { mutableStateOf(initialBuilding.longitude.toString()) }
    var radius by remember { mutableFloatStateOf(initialBuilding.radiusInMeters) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bino Lokatsiyasi va Radiusi",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Hodimlar ushbu radius ichiga kirganda davomatdan o'tishlari mumkin bo'ladi. Xaritani bosib bino o'rnini o'zgartirishingiz mumkin.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Leaflet Interactive Map
        val latDouble = lat.toDoubleOrNull() ?: initialBuilding.latitude
        val lngDouble = lng.toDoubleOrNull() ?: initialBuilding.longitude

        LeafletMapView(
            latitude = latDouble,
            longitude = lngDouble,
            radiusMeters = radius,
            isSelectable = true,
            onLocationSelected = { selectedLat, selectedLng ->
                lat = "%.6f".format(selectedLat).replace(',', '.')
                lng = "%.6f".format(selectedLng).replace(',', '.')
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )

        // GPS orqali hozirgi joylashuvni olish tugmasi
        OutlinedButton(
            onClick = {
                locationHelper.getCurrentLocation { loc: Location? ->
                    if (loc != null) {
                        lat = "%.6f".format(loc.latitude).replace(',', '.')
                        lng = "%.6f".format(loc.longitude).replace(',', '.')
                        Toast.makeText(context, "Hozirgi GPS lokatsiyangiz olindi!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "GPS lokatsiyasini aniqlab bo'lmadi. GPS yoqilganligini tekshiring.", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Mening hozirgi joylashuvimni bino deb olish")
        }

        // Bino nomi
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Bino / Ofis nomi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = lat,
                onValueChange = { lat = it },
                label = { Text("Kenglik (Lat)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = lng,
                onValueChange = { lng = it },
                label = { Text("Uzunlik (Lng)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Radius Slider
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ruxsat etilgan radius:",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${radius.toInt()} metr",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = radius,
                    onValueChange = { radius = it },
                    valueRange = 20f..500f,
                    steps = 19
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SuggestionChip(onClick = { radius = 50f }, label = { Text("50m") })
                    SuggestionChip(onClick = { radius = 100f }, label = { Text("100m") })
                    SuggestionChip(onClick = { radius = 200f }, label = { Text("200m") })
                    SuggestionChip(onClick = { radius = 300f }, label = { Text("300m") })
                }
            }
        }

        Button(
            onClick = {
                val parsedLat = lat.toDoubleOrNull() ?: initialBuilding.latitude
                val parsedLng = lng.toDoubleOrNull() ?: initialBuilding.longitude
                onSaveBuilding(
                    Building(
                        id = initialBuilding.id,
                        name = name.ifBlank { "Bosh Ofis" },
                        latitude = parsedLat,
                        longitude = parsedLng,
                        radiusInMeters = radius
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("O'zgarishlarni Saqlash", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// 3. EMPLOYEES TAB
@Composable
fun AdminEmployeesTab(
    employees: List<User>,
    onDelete: (String) -> Unit
) {
    if (employees.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Hodimlar ro'yxati bo'sh.\nQuyidagi + tugmasi orqali hodim qo'shing.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
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
                    text = "Barcha Hodimlar (${employees.size} ta)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            items(employees) { emp ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emp.name.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = emp.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${emp.position.ifEmpty { "Hodim" }} • ${emp.phone}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Login: ${emp.username} | Parol: ${emp.password}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(onClick = { onDelete(emp.id) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "O'chirish",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

// 4. HISTORY TAB
@Composable
fun AdminHistoryTab(historyRecords: List<AttendanceRecord>) {
    if (historyRecords.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Hozircha davomat tarixi mavjud emas.",
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
                    text = "Barcha Davomat Tarixi",
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rec.userName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = rec.date,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (rec.userPosition.isNotEmpty()) {
                            Text(
                                text = rec.userPosition,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kirgan: ${rec.checkInTime ?: "--"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Chiqgan: ${rec.checkOutTime ?: "Chiqish qayd etilmagan"}",
                                fontSize = 12.sp,
                                color = if (rec.checkOutTime != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Dialog: Add Employee
@Composable
fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onAdd: (User) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+998 ") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yangi Hodim Qo'shish") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("F.I.SH (Ism Sharif)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Lavozimi (masalan: Dasturchi)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefon raqami") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Login") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Parol") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                        onAdd(
                            User(
                                id = UUID.randomUUID().toString(),
                                name = name.trim(),
                                username = username.trim(),
                                password = password.trim(),
                                role = UserRole.HODIM,
                                phone = phone.trim(),
                                position = position.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Qo'shish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        }
    )
}
