package com.example.myapplication.data

import com.example.myapplication.model.AttendanceRecord
import com.example.myapplication.model.Building
import com.example.myapplication.model.User
import com.example.myapplication.model.UserRole
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object SupabaseClient {

    private const val SUPABASE_URL = "https://bmvptuvwksjzbfhoslcl.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_xOn-RHN14uVVSPY2rFBYVg_qgd06cGp"

    private fun createConnection(endpoint: String, method: String): HttpURLConnection {
        val url = URL("$SUPABASE_URL/rest/v1/$endpoint")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.setRequestProperty("apikey", SUPABASE_KEY)
        conn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Prefer", "return=representation,resolution=merge-duplicates")
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        return conn
    }

    private fun executeGet(endpoint: String): String? {
        return try {
            val conn = createConnection(endpoint, "GET")
            val code = conn.responseCode
            if (code in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                sb.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun executePost(endpoint: String, jsonBody: String): Boolean {
        return try {
            val conn = createConnection(endpoint, "POST")
            conn.doOutput = true
            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(jsonBody)
            writer.flush()
            writer.close()
            val code = conn.responseCode
            code in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun executeDelete(endpoint: String): Boolean {
        return try {
            val conn = createConnection(endpoint, "DELETE")
            val code = conn.responseCode
            code in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun fetchUsers(onResult: (List<User>?) -> Unit) {
        thread {
            val jsonStr = executeGet("users?select=*")
            if (jsonStr == null) {
                onResult(null)
                return@thread
            }
            try {
                val list = mutableListOf<User>()
                val jsonArray = JSONArray(jsonStr)
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
                onResult(list)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    fun upsertUser(user: User) {
        thread {
            val obj = JSONObject().apply {
                put("id", user.id)
                put("name", user.name)
                put("username", user.username)
                put("password", user.password)
                put("role", user.role.name)
                put("phone", user.phone)
                put("position", user.position)
            }
            executePost("users", obj.toString())
        }
    }

    fun deleteUser(userId: String) {
        thread {
            executeDelete("users?id=eq.$userId")
        }
    }

    fun fetchBuildings(onResult: (List<Building>?) -> Unit) {
        thread {
            val jsonStr = executeGet("buildings?select=*")
            if (jsonStr == null) {
                onResult(null)
                return@thread
            }
            try {
                val list = mutableListOf<Building>()
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        Building(
                            id = obj.optString("id", "building_main"),
                            name = obj.optString("name", "Bosh Ofis"),
                            latitude = obj.optDouble("latitude", 41.311081),
                            longitude = obj.optDouble("longitude", 69.279737),
                            radiusInMeters = obj.optDouble("radius_in_meters", 100.0).toFloat()
                        )
                    )
                }
                onResult(list)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    fun upsertBuilding(building: Building) {
        thread {
            val obj = JSONObject().apply {
                put("id", building.id)
                put("name", building.name)
                put("latitude", building.latitude)
                put("longitude", building.longitude)
                put("radius_in_meters", building.radiusInMeters.toDouble())
            }
            executePost("buildings", obj.toString())
        }
    }

    fun deleteBuilding(buildingId: String) {
        thread {
            executeDelete("buildings?id=eq.$buildingId")
        }
    }

    fun fetchAttendance(onResult: (List<AttendanceRecord>?) -> Unit) {
        thread {
            val jsonStr = executeGet("attendance_records?select=*")
            if (jsonStr == null) {
                onResult(null)
                return@thread
            }
            try {
                val list = mutableListOf<AttendanceRecord>()
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        AttendanceRecord(
                            id = obj.getString("id"),
                            userId = obj.getString("user_id"),
                            userName = obj.getString("user_name"),
                            userPosition = obj.optString("user_position", ""),
                            date = obj.getString("date"),
                            checkInTime = if (obj.has("check_in_time") && !obj.isNull("check_in_time")) obj.getString("check_in_time") else null,
                            checkOutTime = if (obj.has("check_out_time") && !obj.isNull("check_out_time")) obj.getString("check_out_time") else null,
                            checkInLatitude = if (obj.has("check_in_latitude") && !obj.isNull("check_in_latitude")) obj.getDouble("check_in_latitude") else null,
                            checkInLongitude = if (obj.has("check_in_longitude") && !obj.isNull("check_in_longitude")) obj.getDouble("check_in_longitude") else null,
                            status = obj.optString("status", "Ishda")
                        )
                    )
                }
                onResult(list)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    fun upsertAttendanceRecord(record: AttendanceRecord) {
        thread {
            val obj = JSONObject().apply {
                put("id", record.id)
                put("user_id", record.userId)
                put("user_name", record.userName)
                put("user_position", record.userPosition)
                put("date", record.date)
                put("check_in_time", record.checkInTime ?: JSONObject.NULL)
                put("check_out_time", record.checkOutTime ?: JSONObject.NULL)
                put("check_in_latitude", record.checkInLatitude ?: JSONObject.NULL)
                put("check_in_longitude", record.checkInLongitude ?: JSONObject.NULL)
                put("status", record.status)
            }
            executePost("attendance_records", obj.toString())
        }
    }
}
