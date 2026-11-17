package com.example.myapplication.model

enum class UserRole {
    BOSHLIQ,
    HODIM
}

data class User(
    val id: String,
    val name: String,
    val username: String,
    val password: String,
    val role: UserRole,
    val phone: String = "",
    val position: String = ""
)

data class Building(
    val id: String = "default_building",
    val name: String = "Bosh Ofis",
    val latitude: Double = 41.311081,
    val longitude: Double = 69.279737,
    val radiusInMeters: Float = 100f
)

data class AttendanceRecord(
    val id: String,
    val userId: String,
    val userName: String,
    val userPosition: String = "",
    val date: String, // Format: YYYY-MM-DD
    val checkInTime: String? = null, // Format: HH:mm:ss
    val checkOutTime: String? = null, // Format: HH:mm:ss
    val checkInLatitude: Double? = null,
    val checkInLongitude: Double? = null,
    val status: String = "Kelgan" // Kelgan, Ishda, Ketgan
)
