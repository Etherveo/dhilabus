package com.example.picbus

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Beranda : Fragment() {

    private lateinit var map: MapView
    private var mapReady = false

    private val busMarkers = mutableMapOf<String, Marker>()
    private val handler = Handler(Looper.getMainLooper())
    private val POLLING_INTERVAL = 10_000L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().load(
            requireContext(),
            android.preference.PreferenceManager
                .getDefaultSharedPreferences(requireContext())
        )

        val view = inflater.inflate(R.layout.a_beranda, container, false)

        // Ambil data dari SharedPreferences
        val sharedPref = requireActivity()
            .getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)

        // Restore token ke RetrofitClient
        val savedToken = sharedPref.getString("accsess_token", "") ?: ""
        RetrofitClient.authToken = savedToken

        // Nama user
        val tvNamaUser = view.findViewById<TextView>(R.id.tvNamaUser)
        tvNamaUser.text = sharedPref.getString("userName", "Pengguna")

        // Setup Map
        map = view.findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)
        map.controller.setCenter(GeoPoint(-0.5401, 123.0585))
        mapReady = true

        val btnKampus1to4     = view.findViewById<LinearLayout>(R.id.btnKampus1to4)
        val btnKampus4to1     = view.findViewById<LinearLayout>(R.id.btnKampus4to1)
        val btnLacakBus       = view.findViewById<LinearLayout>(R.id.btnLacakBus)
        val tvNamaBusTerdekat = view.findViewById<TextView>(R.id.tvNamaBusTerdekat)

        btnKampus1to4.setOnClickListener { navigateToJadwal() }
        btnKampus4to1.setOnClickListener { navigateToJadwal() }
        btnLacakBus.setOnClickListener {
            navigateToRute(tvNamaBusTerdekat.text.toString())
        }

        return view
    }

    private val pollingRunnable = object : Runnable {
        override fun run() {
            fetchSemuaPosisiBus()
            handler.postDelayed(this, POLLING_INTERVAL)
        }
    }

    private fun fetchSemuaPosisiBus() {
        RetrofitClient.instance.semuaPosisiBus()
            .enqueue(object : Callback<BusPositionResponse> {
                override fun onResponse(
                    call: Call<BusPositionResponse>,
                    response: Response<BusPositionResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val buses = response.body()!!.buses
                        updateMarkersOnMap(buses)
                    }
                }

                override fun onFailure(call: Call<BusPositionResponse>, t: Throwable) {
                    // Gagal fetch — marker terakhir tetap tampil
                }
            })
    }


    /**
     * Membuat ikon marker berbentuk pin dengan label nama bus (A, B, C, D, dst.)
     * agar setiap titik lokasi di peta dapat dikenali bus mana yang menyalakannya.
     */
    private fun createBusMarkerIcon(namaBus: String): BitmapDrawable {
        // Ekstrak huruf singkat dari nama bus: "Bus A" -> "A", "BUS B" -> "B"
        val label = namaBus.trim().uppercase().let { name ->
            val words = name.split(" ")
            words.lastOrNull()?.take(2) ?: name.take(2)
        }

        val size          = 120
        val circleR       = 42f
        val pinTipY       = size.toFloat() - 6f
        val centerX       = size / 2f
        val circleTopY    = 12f
        val circleCenterY = circleTopY + circleR

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Bayangan lembut di bawah pin
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 0, 0, 0)
        }
        canvas.drawOval(
            RectF(centerX - 18f, pinTipY - 6f, centerX + 18f, pinTipY + 6f),
            shadowPaint
        )

        // Warna pin berbeda per bus (A=Biru, B=Hijau, C=Oranye, D=Ungu, lainnya=Abu)
        val pinColor = when (label.firstOrNull()) {
            'A'  -> Color.parseColor("#1565C0")
            'B'  -> Color.parseColor("#2E7D32")
            'C'  -> Color.parseColor("#E65100")
            'D'  -> Color.parseColor("#6A1B9A")
            else -> Color.parseColor("#37474F")
        }

        val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pinColor }

        // Batang pin (segitiga ke bawah)
        val path = Path().apply {
            moveTo(centerX - 14f, circleCenterY + circleR - 6f)
            lineTo(centerX + 14f, circleCenterY + circleR - 6f)
            lineTo(centerX, pinTipY)
            close()
        }
        canvas.drawPath(path, pinPaint)

        // Lingkaran kepala pin
        canvas.drawCircle(centerX, circleCenterY, circleR, pinPaint)

        // Border putih tipis
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color       = Color.WHITE
            style       = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawCircle(centerX, circleCenterY, circleR - 2f, borderPaint)

        // Teks label bus di tengah lingkaran
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color     = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface  = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textSize  = if (label.length > 1) 28f else 34f
        }
        val textY = circleCenterY - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(label, centerX, textY, textPaint)

        return BitmapDrawable(resources, bitmap)
    }

    private fun updateMarkersOnMap(buses: List<BusPosition>) {
        if (!isAdded || !mapReady) return

        buses.forEach { bus ->
            val lat = bus.latitude
            val lng = bus.longitude
            val key = bus.namaBus

            if (bus.isOnline && lat != null && lng != null) {
                val point = GeoPoint(lat, lng)
                val existing = busMarkers[key]

                if (existing != null) {
                    existing.position = point
                } else {
                    val marker = Marker(map)
                    marker.position = point
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title   = key
                    marker.snippet = "$key • Online"
                    // Ganti ikon default (telunjuk) dengan pin berlabel nama bus
                    marker.icon    = createBusMarkerIcon(key)

                    marker.setOnMarkerClickListener { m, _ ->
                        navigateToRute(m.title ?: "")
                        true
                    }

                    map.overlays.add(marker)
                    busMarkers[key] = marker
                }
            } else {
                busMarkers[key]?.let { marker ->
                    map.overlays.remove(marker)
                }
                busMarkers.remove(key)
            }
        }
        map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        if (::map.isInitialized) map.onResume()
        handler.post(pollingRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (::map.isInitialized) map.onPause()
        handler.removeCallbacks(pollingRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(pollingRunnable)
        mapReady = false
    }

    private fun navigateToRute(busName: String) {
        val fragment = Rute()
        val bundle   = Bundle()
        bundle.putString("bus_type", busName)
        fragment.arguments = bundle

        val homeActivity = activity as? HomeActivity
        homeActivity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.addToBackStack(null)
            ?.commit()

        homeActivity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            ?.selectedItemId = R.id.nav_rute
    }

    private fun navigateToJadwal() {
        val homeActivity = activity as? HomeActivity
        homeActivity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, JadwalBus())
            ?.addToBackStack(null)
            ?.commit()

        homeActivity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            ?.selectedItemId = R.id.nav_jad
    }
}