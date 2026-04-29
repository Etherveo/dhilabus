package com.example.picbus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class Logout : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.a_profil, container, false)

        // Inisialisasi View
        val tvNama = view.findViewById<TextView>(R.id.tvNamaProfil)
        val tvNim = view.findViewById<TextView>(R.id.tvNimProfil)
        val menuLogout = view.findViewById<LinearLayout>(R.id.menuLogout)
        val menuHelp = view.findViewById<LinearLayout>(R.id.menuHelp)

        // Ambil Data dari SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        tvNama.text = sharedPref.getString("userName", "Nama Pengguna")
        tvNim.text = "NIM: " + sharedPref.getString("userNim", "-")


        // Logika Menu Bantuan
        menuHelp.setOnClickListener {
            val fragment = Bantuan()
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, fragment)
                ?.addToBackStack(null)
                ?.commit()
        }

        // Logika Logout
        menuLogout.setOnClickListener {
            showLogoutDialog()
        }

        return view
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
            .setPositiveButton("Ya") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        val sharedPref = requireActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        Toast.makeText(context, "Berhasil Keluar", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}