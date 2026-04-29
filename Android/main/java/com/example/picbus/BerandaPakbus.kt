package com.example.picbus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BerandaPakbus : AppCompatActivity() {

    private lateinit var switchLiveLocation: SwitchCompat
    private lateinit var tvStatusLabel: TextView
    private lateinit var btnLogout: MaterialButton

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_home_pakbus)

        // Restore token dari SharedPreferences ke RetrofitClient
        val sharedPref = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val savedToken = sharedPref.getString("accsess_token", "") ?: ""
        RetrofitClient.authToken = savedToken

        switchLiveLocation = findViewById(R.id.switchLiveLocation)
        tvStatusLabel      = findViewById(R.id.tvStatusLiveLocation)
        btnLogout          = findViewById(R.id.btnLogoutPakbus)

        val isActive = sharedPref.getBoolean("isLocationActive", false)
        switchLiveLocation.isChecked = isActive
        updateStatusLabel(isActive)

        switchLiveLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) requestLocationPermissionThenStart()
            else stopLocationSharing()
        }

        btnLogout.setOnClickListener {
            stopLocationSharing()
            sharedPref.edit().clear().apply()
            RetrofitClient.authToken = ""
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun requestLocationPermissionThenStart() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationSharing()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationSharing()
        } else {
            switchLiveLocation.isChecked = false
            Toast.makeText(this, "Izin lokasi diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLocationSharing() {
        try {
            val serviceIntent = Intent(this, LokasiBus::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)

            // TAMBAHKAN INI — beritahu server bus aktif
            RetrofitClient.instance.toggleAktif(1).enqueue(object : Callback<ToggleAktifResponse> {
                override fun onResponse(call: Call<ToggleAktifResponse>, response: Response<ToggleAktifResponse>) {
                    android.util.Log.d("PAKBUS", "Toggle aktif: ${response.body()?.message}")
                }
                override fun onFailure(call: Call<ToggleAktifResponse>, t: Throwable) {
                    android.util.Log.e("PAKBUS", "Gagal toggle aktif: ${t.message}")
                }
            })

            getSharedPreferences("USER_DATA", MODE_PRIVATE)
                .edit().putBoolean("isLocationActive", true).apply()
            updateStatusLabel(true)
            Toast.makeText(this, "Lokasi aktif dibagikan", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            switchLiveLocation.isChecked = false
            Toast.makeText(this, "Gagal memulai layanan lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLocationSharing() {
        try {
            stopService(Intent(this, LokasiBus::class.java))

            // TAMBAHKAN INI — beritahu server bus nonaktif
            RetrofitClient.instance.toggleAktif(0).enqueue(object : Callback<ToggleAktifResponse> {
                override fun onResponse(call: Call<ToggleAktifResponse>, response: Response<ToggleAktifResponse>) {
                    android.util.Log.d("PAKBUS", "Toggle nonaktif: ${response.body()?.message}")
                }
                override fun onFailure(call: Call<ToggleAktifResponse>, t: Throwable) {
                    android.util.Log.e("PAKBUS", "Gagal toggle nonaktif: ${t.message}")
                }
            })
        } catch (e: Exception) { }

        getSharedPreferences("USER_DATA", MODE_PRIVATE)
            .edit().putBoolean("isLocationActive", false).apply()
        updateStatusLabel(false)
    }

    private fun updateStatusLabel(isActive: Boolean) {
        tvStatusLabel.text = if (isActive) "Aktif" else "Tidak Aktif"
        tvStatusLabel.setTextColor(
            if (isActive) ContextCompat.getColor(this, android.R.color.holo_green_dark)
            else ContextCompat.getColor(this, android.R.color.holo_red_dark)
        )
    }
}