package com.example.picbus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LokasiBus : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val CHANNEL_ID = "BUS_LOCATION_CHANNEL"

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ambil token dari SharedPreferences — WAJIB sebelum kirim request
        val savedToken = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
            .getString("accsess_token", "") ?: ""
        RetrofitClient.authToken = savedToken

        android.util.Log.d("LOKASIBUS", "Token loaded: ${if (savedToken.isNotEmpty()) "ADA" else "KOSONG"}")

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Picbus Driver Aktif")
            .setContentText("Lokasi Anda sedang dibagikan secara real-time")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        android.util.Log.d("LOKASIBUS", "Service started, memulai location updates...")
        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        )
            .setMinUpdateIntervalMillis(5_000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                android.util.Log.d("LOKASIBUS", "GPS dapat: ${location.latitude}, ${location.longitude}")
                kirimKoordinat(location.latitude, location.longitude)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            android.util.Log.d("LOKASIBUS", "Location updates berhasil dimulai")
        } catch (e: SecurityException) {
            android.util.Log.e("LOKASIBUS", "Permission GPS tidak ada: ${e.message}")
            stopSelf()
        }
    }

    private fun kirimKoordinat(lat: Double, lng: Double) {
        android.util.Log.d("LOKASIBUS", "Mengirim koordinat: $lat, $lng | Token: ${RetrofitClient.authToken.take(10)}...")

        RetrofitClient.instance.updateKoordinat(lat, lng)
            .enqueue(object : Callback<UpdateKoordinatResponse> {
                override fun onResponse(
                    call: Call<UpdateKoordinatResponse>,
                    response: Response<UpdateKoordinatResponse>
                ) {
                    if (response.isSuccessful) {
                        android.util.Log.d("LOKASIBUS", "✓ Koordinat berhasil dikirim")
                    } else {
                        android.util.Log.e("LOKASIBUS", "✗ Gagal: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UpdateKoordinatResponse>, t: Throwable) {
                    android.util.Log.e("LOKASIBUS", "✗ Error koneksi: ${t.message}")
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            android.util.Log.d("LOKASIBUS", "Service dihentikan")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bus Location Sharing",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel untuk berbagi lokasi bus secara real-time"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}