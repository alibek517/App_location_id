package com.example.myapplication.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletMapView(
    latitude: Double,
    longitude: Double,
    radiusMeters: Float,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    isSelectable: Boolean = false,
    onLocationSelected: ((Double, Double) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val jsBridge = remember(onLocationSelected) {
        object {
            @JavascriptInterface
            fun onMapClick(lat: Double, lng: Double) {
                onLocationSelected?.invoke(lat, lng)
            }
        }
    }

    val html = remember(latitude, longitude, radiusMeters, userLatitude, userLongitude, isSelectable) {
        val userMarkerJs = if (userLatitude != null && userLongitude != null) {
            """
            var userIcon = L.divIcon({
                className: 'custom-user-icon',
                html: '<div style="background-color:#2563EB; width:16px; height:16px; border-radius:50%; border:3px solid white; box-shadow:0 0 8px rgba(0,0,0,0.4);"></div>',
                iconSize: [16, 16],
                iconAnchor: [8, 8]
            });
            var userMarker = L.marker([$userLatitude, $userLongitude], {icon: userIcon}).addTo(map);
            userMarker.bindPopup("<b>Sizning joylashuvingiz</b>");
            """.trimIndent()
        } else ""

        val clickJs = if (isSelectable) {
            """
            map.on('click', function(e) {
                var lat = e.latlng.lat;
                var lng = e.latlng.lng;
                buildingMarker.setLatLng([lat, lng]);
                buildingCircle.setLatLng([lat, lng]);
                if (window.AndroidBridge) {
                    window.AndroidBridge.onMapClick(lat, lng);
                }
            });
            """.trimIndent()
        } else ""

        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body, html, #map { margin: 0; padding: 0; width: 100%; height: 100%; background: #f3f4f6; }
                .leaflet-control-attribution { font-size: 8px !important; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    zoomControl: true,
                    attributionControl: false
                }).setView([$latitude, $longitude], 16);

                L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                }).addTo(map);

                // Building Marker
                var buildingMarker = L.marker([$latitude, $longitude]).addTo(map);
                buildingMarker.bindPopup("<b>🏢 Bino markazi</b><br>Radius: ${radiusMeters.toInt()}m").openPopup();

                // Geofence Circle
                var buildingCircle = L.circle([$latitude, $longitude], {
                    color: '#EF4444',
                    fillColor: '#F87171',
                    fillOpacity: 0.25,
                    radius: $radiusMeters
                }).addTo(map);

                $userMarkerJs
                $clickJs
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()
                    addJavascriptInterface(jsBridge, "AndroidBridge")
                    loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
