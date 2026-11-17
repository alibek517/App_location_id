package com.example.myapplication.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.myapplication.model.Building

class LocationHelper(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            onLocationReceived(null)
            return
        }

        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        var lastKnown: Location? = null
        if (gpsEnabled) {
            lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
        if (lastKnown == null && networkEnabled) {
            lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        if (lastKnown != null) {
            onLocationReceived(lastKnown)
            return
        }

        // If no cached location, request single update
        val provider = when {
            gpsEnabled -> LocationManager.GPS_PROVIDER
            networkEnabled -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (provider != null) {
            try {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        onLocationReceived(location)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }
                locationManager.requestSingleUpdate(provider, listener, null)
            } catch (e: Exception) {
                onLocationReceived(null)
            }
        } else {
            onLocationReceived(null)
        }
    }

    companion object {
        fun calculateDistanceMeters(
            startLat: Double,
            startLng: Double,
            endLat: Double,
            endLng: Double
        ): Float {
            val results = FloatArray(1)
            Location.distanceBetween(startLat, startLng, endLat, endLng, results)
            return results[0]
        }

        fun isInsideGeofence(
            userLat: Double,
            userLng: Double,
            building: Building
        ): Boolean {
            val distance = calculateDistanceMeters(
                userLat,
                userLng,
                building.latitude,
                building.longitude
            )
            return distance <= building.radiusInMeters
        }
    }
}
