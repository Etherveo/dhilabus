package com.example.picbus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JadwalAdapter(
    private val jadwalList: List<Jadwal>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<JadwalAdapter.JadwalViewHolder>() {

    class JadwalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val waktuTextView: TextView = view.findViewById(R.id.tv_waktu)
        val keteranganTextView: TextView = view.findViewById(R.id.tv_keterangan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JadwalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jadwal, parent, false)
        return JadwalViewHolder(view)
    }

    override fun onBindViewHolder(holder: JadwalViewHolder, position: Int) {
        val jadwal = jadwalList[position]
        holder.waktuTextView.text = jadwal.waktu
        holder.keteranganTextView.text = jadwal.keterangan

        holder.itemView.setOnClickListener {
            // Ambil nama bus saja (misal "Bus A") dari keterangan
            val busName = jadwal.keterangan.split(" • ")[0]
            onItemClick(busName)
        }
    }

    override fun getItemCount() = jadwalList.size
}