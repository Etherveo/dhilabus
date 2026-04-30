package com.example.picbus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
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

    // Variabel untuk Simulasi Manual
    private lateinit var spinnerManual: Spinner
    private lateinit var btnHapusLokasi: Button

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    // Daftar koordinat statis (Sesuaikan dengan data dari Seeder / Database)
    private val daftarTitik = listOf(
        TitikSimulasi("Pilih Lokasi Manual...", 0.0, 0.0),
        TitikSimulasi("Kampus 1 UNG", -0.5420, 123.0592),
        TitikSimulasi("Bundaran HI", -0.5401, 123.0585),
        TitikSimulasi("Gerbang Kampus 4", -0.5512, 123.0610),
        TitikSimulasi("Gedung Rektorat K4", -0.5530, 123.0625)
    )

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

        spinnerManual      = findViewById(R.id.spinnerLokasiManual)
        btnHapusLokasi     = findViewById(R.id.btnHapusLokasi)

        // Set status awal switch dari SharedPreferences
        val isActive = sharedPref.getBoolean("isLocationActive", false)
        switchLiveLocation.isChecked = isActive
        updateStatusLabel(isActive)

        // Setup dropdown spinner
        setupSpinner()

        switchLiveLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestLocationPermissionThenStart()
            } else {
                stopLocationSharing()
            }
        }

        // Listener tombol reset lokasi manual
        btnHapusLokasi.setOnClickListener {
            resetKeGpsAsli()
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

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            daftarTitik.map { it.nama }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerManual.adapter = adapter

        spinnerManual.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    // Validasi: Bus harus dalam keadaan "Aktif" dulu sebelum bisa kirim manual
                    if (!switchLiveLocation.isChecked) {
                        Toast.makeText(this@BerandaPakbus, "Aktifkan Bus terlebih dahulu!", Toast.LENGTH_SHORT).show()
                        spinnerManual.setSelection(0)
                        return
                    }
                    val titik = daftarTitik[position]
                    kirimLokasiManual(titik)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun kirimLokasiManual(titik: TitikSimulasi) {
        // 1. MATIKAN LIVE GPS SERVICE agar koordinat HP tidak menimpa simulasi
        try {
            stopService(Intent(this, LokasiBus::class.java))
        } catch (e: Exception) {}

        // 2. Tampilkan tombol reset
        btnHapusLokasi.visibility = View.VISIBLE

        // 3. Tembak API koordinat manual
        RetrofitClient.instance.updateKoordinat(titik.lat, titik.lng)
            .enqueue(object : Callback<UpdateKoordinatResponse> {
                override fun onResponse(call: Call<UpdateKoordinatResponse>, response: Response<UpdateKoordinatResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@BerandaPakbus, "Lokasi diset ke ${titik.nama}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<UpdateKoordinatResponse>, t: Throwable) {
                    Toast.makeText(this@BerandaPakbus, "Gagal kirim lokasi manual", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun resetKeGpsAsli() {
        // Reset tampilan dropdown dan sembunyikan tombol
        spinnerManual.setSelection(0)
        btnHapusLokasi.visibility = View.GONE

        // Jika status bus masih "Aktif", nyalakan kembali GPS Real-time
        if (switchLiveLocation.isChecked) {
            requestLocationPermissionThenStart()
            Toast.makeText(this, "Kembali menggunakan GPS Real-time", Toast.LENGTH_SHORT).show()
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
            // Cek apakah sedang pakai mode manual, jika ya jangan jalankan service GPS
            if (spinnerManual.selectedItemPosition == 0) {
                val serviceIntent = Intent(this, LokasiBus::class.java)
                ContextCompat.startForegroundService(this, serviceIntent)
            }

            // Beritahu server bus aktif
            RetrofitClient.instance.toggleAktif(1).enqueue(object : Callback<ToggleAktifResponse> {
                override fun onResponse(call: Call<ToggleAktifResponse>, response: Response<ToggleAktifResponse>) {
                    android.util.Log.d("PAKBUS", "Toggle aktif: ${response.body()?.message}")
                    if (spinnerManual.selectedItemPosition > 0) {
                        kirimLokasiManual(daftarTitik[spinnerManual.selectedItemPosition])
                    }
                }
                override fun onFailure(call: Call<ToggleAktifResponse>, t: Throwable) {
                    android.util.Log.e("PAKBUS", "Gagal toggle aktif: ${t.message}")
                }
            })

            getSharedPreferences("USER_DATA", MODE_PRIVATE)
                .edit().putBoolean("isLocationActive", true).apply()
            updateStatusLabel(true)

            if (spinnerManual.selectedItemPosition == 0) {
                Toast.makeText(this, "Lokasi aktif dibagikan", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            switchLiveLocation.isChecked = false
            Toast.makeText(this, "Gagal memulai layanan lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLocationSharing() {
        try {
            // Hentikan Service GPS
            stopService(Intent(this, LokasiBus::class.java))

            // Beritahu server bus nonaktif
            RetrofitClient.instance.toggleAktif(0).enqueue(object : Callback<ToggleAktifResponse> {
                override fun onResponse(call: Call<ToggleAktifResponse>, response: Response<ToggleAktifResponse>) {
                    android.util.Log.d("PAKBUS", "Toggle nonaktif: ${response.body()?.message}")
                }
                override fun onFailure(call: Call<ToggleAktifResponse>, t: Throwable) {
                    android.util.Log.e("PAKBUS", "Gagal toggle nonaktif: ${t.message}")
                }
            })
        } catch (e: Exception) { }

        // Reset state penyimpanan dan UI
        getSharedPreferences("USER_DATA", MODE_PRIVATE)
            .edit().putBoolean("isLocationActive", false).apply()
        updateStatusLabel(false)

        // Reset manual dropdown jika bus dimatikan
        spinnerManual.setSelection(0)
        btnHapusLokasi.visibility = View.GONE
    }

    private fun updateStatusLabel(isActive: Boolean) {
        tvStatusLabel.text = if (isActive) "Aktif" else "Tidak Aktif"
        tvStatusLabel.setTextColor(
            if (isActive) ContextCompat.getColor(this, android.R.color.holo_green_dark)
            else ContextCompat.getColor(this, android.R.color.holo_red_dark)
        )
    }
}

// Data class untuk daftar lokasi simulasi
data class TitikSimulasi(val nama: String, val lat: Double, val lng: Double)