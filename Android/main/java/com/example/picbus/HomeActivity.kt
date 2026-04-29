package com.example.picbus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottomNav = findViewById(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            loadFragment(Beranda())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            
            // LOGIKA PENTING: Jika kita sudah di Bantuan, jangan timpa dengan profil saat nav_profil diklik otomatis
            when (item.itemId) {
                R.id.nav_hom -> loadFragment(Beranda())
                R.id.nav_jad -> loadFragment(JadwalBus())
                R.id.nav_rute -> loadFragment(Rute())
                R.id.nav_profil -> {
                    // Hanya load profil jika fragment saat ini bukan Bantuan
                    if (currentFragment !is Bantuan) {
                        loadFragment(Logout())
                    }
                }
            }
            true
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Fungsi navigasi khusus tanpa memicu listener nav_profil secara berlebihan
    fun navigateToSpecificFragment(fragment: Fragment, navId: Int) {
        // Matikan listener sementara agar tidak terjadi loop/timpa
        bottomNav.setOnItemSelectedListener(null)
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
            
        bottomNav.selectedItemId = navId
        
        // Aktifkan kembali listener
        reassignNavListener()
    }

    private fun reassignNavListener() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_hom -> loadFragment(Beranda())
                R.id.nav_jad -> loadFragment(JadwalBus())
                R.id.nav_rute -> loadFragment(Rute())
                R.id.nav_profil -> loadFragment(Logout())
            }
            true
        }
    }
}