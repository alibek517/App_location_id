package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.model.AttendanceRecord
import com.example.myapplication.model.Building
import com.example.myapplication.model.User
import com.example.myapplication.model.UserRole
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smart_attendance_prefs", Context.MODE_PRIVATE)

    init {
        initInitialDataIfEmpty()
    }

    private fun initInitialDataIfEmpty() {
        if (!prefs.contains(KEY_USERS)) {
            val initialUsers = listOf(
                User(
                    id = "admin_1",
                    name = "Akbar Rahimov",
                    username = "admin",
                    password = "123",
                    role = UserRole.BOSHLIQ,
                    phone = "+998 90 123 45 67",
                    position = "Bosh direktor"
                ),
                User(
                    id = "hodim_1",
                    name = "Jasur Aliyev",
                    username = "jasur",
                    password = "123",
                    role = UserRole.HODIM,
                    phone = "+998 93 111 22 33",
                    position = "Bosh dasturchi"
                ),
                User(
                    id = "hodim_2",
                    name = "Madina Karimova",
                    username = "madina",
                    password = "123",
                    role = UserRole.HODIM,
                    phone = "+998 97 444 55 66",
                    position = "HR Menejer"
                ),
                User(
                    id = "hodim_3",
                    name = "Sardor Umarov",
                    username = "sardor",
                    password = "123",
                    role = UserRole.HODIM,
                    phone = "+998 99 777 88 99",
                    position = "Hisobchi"
                )
            )
            saveUsers(initialUsers)
        }

        if (!prefs.contains(KEY_BUILDING)) {
            // Default: Toshkent markazi (Amir Temur xiyoboni yaqini)
            val defaultBuilding = Building(
                id = "building_main",
                name = "Markaziy Bosh Ofis",
                latitude = 41.311081,
                longitude = 69.279737,
                radiusInMeters = 100f
            )
            saveBuilding(defaultBuilding)
        }

        if (!prefs.contains(KEY_ATTENDANCE)) {
            // Namunaviy tarix yozuvlari
            val sampleAttendance = listOf(
                AttendanceRecord(
                    id = UUID.randomUUID().toString(),
                    userId = "hodim_1",
                    userName = "Jasur Aliyev",
                    userPosition = "Bosh dasturchi",
                    date = getTodayDateString(),
                    checkInTime = "08:52:14",
                    checkOutTime = null,
                    checkInLatitude = 41.311085,
                    checkInLongitude = 69.279740,
                    status = "Ishda"
                ),
                AttendanceRecord(
                    id = UUID.randomUUID().toString(),
                    userId = "hodim_2",
                    userName = "Madina Karimova",
                    userPosition = "HR Menejer",
                    date = getTodayDateString(),
                    checkInTime = "09:05:30",
                    checkOutTime = "18:00:10",
                    checkInLatitude = 41.311090,
                    checkInLongitude = 69.279735,
                    status = "Ketgan"
                )
            )
            saveAttendanceList(sampleAttendance)
        }
    }

    // --- USERS ---
    fun getUsers(): List<User> {
        val jsonString = prefs.getString(KEY_USERS, null) ?: return emptyList()
        val list = mutableListOf<User>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    User(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        username = obj.getString("username"),
                        password = obj.getString("password"),
                        role = UserRole.valueOf(obj.getString("role")),
                        phone = obj.optString("phone", ""),
                        position = obj.optString("position", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveUsers(users: List<User>) {
        val jsonArray = JSONArray()
        for (u in users) {
            val obj = JSONObject().apply {
                put("id", u.id)
                put("name", u.name)
                put("username", u.username)
                put("password", u.password)
                put("role", u.role.name)
                put("phone", u.phone)
                put("position", u.position)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_USERS, jsonArray.toString()).apply()
    }

    fun addUser(user: User): Boolean {
        val users = getUsers().toMutableList()
        if (users.any { it.username.equals(user.username, ignoreCase = true) }) {
            return false // username allaqachon mavjud
        }
        users.add(user)
        saveUsers(users)
        return true
    }

    fun deleteUser(userId: String) {
        val users = getUsers().filter { it.id != userId }
        saveUsers(users)
    }

    fun login(username: String, password: String): User? {
        val users = getUsers()
        return users.firstOrNull {
            it.username.equals(username.trim(), ignoreCase = true) && it.password == password.trim()
        }
    }

    // --- BUILDING ---
    fun getBuilding(): Building {
        val jsonString = prefs.getString(KEY_BUILDING, null) ?: return Building()
        return try {
            val obj = JSONObject(jsonString)
            Building(
                id = obj.optString("id", "building_main"),
                name = obj.optString("name", "Bosh Ofis"),
                latitude = obj.optDouble("latitude", 41.311081),
                longitude = obj.optDouble("longitude", 69.279737),
                radiusInMeters = obj.optDouble("radiusInMeters", 100.0).toFloat()
            )
        } catch (e: Exception) {
            Building()
        }
    }

    fun saveBuilding(building: Building) {
        val obj = JSONObject().apply {
            put("id", building.id)
            put("name", building.name)
            put("latitude", building.latitude)
            put("longitude", building.longitude)
            put("radiusInMeters", building.radiusInMeters.toDouble())
        }
        prefs.edit().putString(KEY_BUILDING, obj.toString()).apply()
    }

    // --- ATTENDANCE ---
    fun getAllAttendance(): List<AttendanceRecord> {
        val jsonString = prefs.getString(KEY_ATTENDANCE, null) ?: return emptyList()
        val list = mutableListOf<AttendanceRecord>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    AttendanceRecord(
                        id = obj.getString("id"),
                        userId = obj.getString("userId"),
                        userName = obj.getString("userName"),
                        userPosition = obj.optString("userPosition", ""),
                        date = obj.getString("date"),
                        checkInTime = if (obj.has("checkInTime") && !obj.isNull("checkInTime")) obj.getString("checkInTime") else null,
                        checkOutTime = if (obj.has("checkOutTime") && !obj.isNull("checkOutTime")) obj.getString("checkOutTime") else null,
                        checkInLatitude = if (obj.has("checkInLatitude") && !obj.isNull("checkInLatitude")) obj.getDouble("checkInLatitude") else null,
                        checkInLongitude = if (obj.has("checkInLongitude") && !obj.isNull("checkInLongitude")) obj.getDouble("checkInLongitude") else null,
                        status = obj.optString("status", "Kelgan")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveAttendanceList(records: List<AttendanceRecord>) {
        val jsonArray = JSONArray()
        for (r in records) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("userId", r.userId)
                put("userName", r.userName)
                put("userPosition", r.userPosition)
                put("date", r.date)
                put("checkInTime", r.checkInTime ?: JSONObject.NULL)
                put("checkOutTime", r.checkOutTime ?: JSONObject.NULL)
                put("checkInLatitude", r.checkInLatitude ?: JSONObject.NULL)
                put("checkInLongitude", r.checkInLongitude ?: JSONObject.NULL)
                put("status", r.status)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_ATTENDANCE, jsonArray.toString()).apply()
    }

    fun getTodayRecord(userId: String): AttendanceRecord? {
        val today = getTodayDateString()
        return getAllAttendance().firstOrNull { it.userId == userId && it.date == today }
    }

    fun checkIn(
        user: User,
        latitude: Double,
        longitude: Double
    ): AttendanceRecord {
        val today = getTodayDateString()
        val timeNow = getCurrentTimeString()
        val records = getAllAttendance().toMutableList()

        val existingIndex = records.indexOfFirst { it.userId == user.id && it.date == today }
        val record = if (existingIndex != -1) {
            val old = records[existingIndex]
            old.copy(
                checkInTime = old.checkInTime ?: timeNow,
                checkInLatitude = latitude,
                checkInLongitude = longitude,
                status = "Ishda"
            )
        } else {
            AttendanceRecord(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                userName = user.name,
                userPosition = user.position,
                date = today,
                checkInTime = timeNow,
                checkOutTime = null,
                checkInLatitude = latitude,
                checkInLongitude = longitude,
                status = "Ishda"
            )
        }

        if (existingIndex != -1) {
            records[existingIndex] = record
        } else {
            records.add(0, record)
        }
        saveAttendanceList(records)
        return record
    }

    fun checkOut(userId: String): AttendanceRecord? {
        val today = getTodayDateString()
        val timeNow = getCurrentTimeString()
        val records = getAllAttendance().toMutableList()

        val existingIndex = records.indexOfFirst { it.userId == userId && it.date == today }
        if (existingIndex != -1) {
            val old = records[existingIndex]
            val updated = old.copy(
                checkOutTime = timeNow,
                status = "Ketgan"
            )
            records[existingIndex] = updated
            saveAttendanceList(records)
            return updated
        }
        return null
    }

    fun getTodayAttendance(): List<AttendanceRecord> {
        val today = getTodayDateString()
        return getAllAttendance().filter { it.date == today }
    }

    fun getUserHistory(userId: String): List<AttendanceRecord> {
        return getAllAttendance().filter { it.userId == userId }.sortedByDescending { it.date + (it.checkInTime ?: "") }
    }

    // --- SESSION ---
    fun getCurrentLoggedInUser(): User? {
        val userId = prefs.getString(KEY_CURRENT_USER_ID, null) ?: return null
        return getUsers().firstOrNull { it.id == userId }
    }

    fun setCurrentLoggedInUser(user: User?) {
        if (user == null) {
            prefs.edit().remove(KEY_CURRENT_USER_ID).apply()
        } else {
            prefs.edit().putString(KEY_CURRENT_USER_ID, user.id).apply()
        }
    }

    companion object {
        private const val KEY_USERS = "users_list_v1"
        private const val KEY_BUILDING = "building_info_v1"
        private const val KEY_ATTENDANCE = "attendance_records_v1"
        private const val KEY_CURRENT_USER_ID = "current_user_id"

        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        fun getCurrentTimeString(): String {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}
