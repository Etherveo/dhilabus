package com.example.picbus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class JadwalBus : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.a_jadwalkeseluruhan, container, false)

        // Setup Tanggal Hari Ini untuk Header
        val tvTanggal = view.findViewById<TextView>(R.id.tv_hari_ini)
        val sdf = SimpleDateFormat("EEEE, dd MMMM", Locale("id", "ID"))
        val tanggalSekarang = sdf.format(Date())
        tvTanggal?.text = "Hari ini, $tanggalSekarang"

        // JADWAL STANDAR (SENIN - KAMIS)
        val jadwalKampus1 = listOf(
            Jadwal("06:45", "Bus A • Gerbang Utama Kampus 1"),
            Jadwal("07:45", "Bus B • Rektorat"),
            Jadwal("08:45", "Bus C • Rektorat"),
            Jadwal("09:45", "Bus D • Rektorat"),
            Jadwal("10:45", "Bus B • Rektorat"),
            Jadwal("11:45", "Bus A • Rektorat"),
            Jadwal("12:45", "Bus C • Rektorat"),
            Jadwal("13:45", "Bus D • Rektorat"),
            Jadwal("14:30", "Bus B • Rektorat"),
            Jadwal("15:30", "Bus A • Rektorat"),
            Jadwal("16:45", "Bus C • Rektorat"),
        )

        val jadwalKampus4 = listOf(
            Jadwal("06:20", "Bus C • Halte Kampus 4"),
            Jadwal("06:35", "Bus D • Fakultas MIPA"),
            Jadwal("08:30", "Bus A • Fakultas MIPA"),
            Jadwal("09:45", "Bus B • Fakultas MIPA"),
            Jadwal("10:30", "Bus C • Fakultas MIPA"),
            Jadwal("11:00", "Bus D • Fakultas MIPA"),
            Jadwal("12:30", "Bus B • Fakultas MIPA"),
            Jadwal("13:00", "Bus A • Fakultas MIPA"),
            Jadwal("14:30", "Bus C • Fakultas MIPA"),
            Jadwal("15:00", "Bus D • Fakultas MIPA"),
            Jadwal("16:00", "Bus B • Fakultas MIPA"),
            Jadwal("16:45", "Bus A • Fakultas MIPA"),
            Jadwal("17:45", "Bus C • Halte Kampus 4"),
        )

        // Set ke RecyclerView 1
        val recyclerView1 = view.findViewById<RecyclerView>(R.id.recyclerView1)
        recyclerView1.layoutManager = LinearLayoutManager(context)
        recyclerView1.adapter = JadwalAdapter(jadwalKampus1) { busName ->
            navigateToBantuan(busName)
        }

        // Set ke RecyclerView 2
        val recyclerView2 = view.findViewById<RecyclerView>(R.id.recyclerView2)
        recyclerView2.layoutManager = LinearLayoutManager(context)
        recyclerView2.adapter = JadwalAdapter(jadwalKampus4) { busName ->
            navigateToBantuan(busName)
        }

        return view
    }

    private fun navigateToBantuan(busName: String) {
        val fragment = Bantuan()
        val bundle = Bundle()
        bundle.putString("bus_type", busName)
        fragment.arguments = bundle

        val homeActivity = activity as? HomeActivity
        
        // Memastikan fragment Bantuan dimuat sebagai konten utama
        homeActivity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container, fragment)
            ?.addToBackStack(null) // Menambahkan ke backstack agar bisa kembali ke jadwal
            ?.commit()

    }
}