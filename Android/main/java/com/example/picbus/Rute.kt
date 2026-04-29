package com.example.picbus

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Rute : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private val POLLING_INTERVAL = 10_000L
    private var busName: String = ""
    private lateinit var adapter: HalteAdapter

    // Simpan list halte terakhir untuk mencegah reset warna
    private val lewatSet = mutableSetOf<Int>() // urutan halte yang sudah dilewati

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.a_rute, container, false)
        busName = arguments?.getString("bus_type") ?: ""

        val tvBusName    = view.findViewById<TextView>(R.id.tvNamaBus)
        val tvEstimasi   = view.findViewById<TextView>(R.id.tvEstimasi)
        val tvJarak      = view.findViewById<TextView>(R.id.tvJarak)
        val tvHalteCount = view.findViewById<TextView>(R.id.tvJumlahHalte)
        val rvHalte      = view.findViewById<RecyclerView>(R.id.rvHalte)

        tvBusName.text = busName

        adapter = HalteAdapter(emptyList())
        rvHalte.layoutManager = LinearLayoutManager(requireContext())
        rvHalte.adapter = adapter

        // Polling setiap 10 detik
        val pollingRunnable = object : Runnable {
            override fun run() {
                fetchRute(tvEstimasi, tvJarak, tvHalteCount)
                handler.postDelayed(this, POLLING_INTERVAL)
            }
        }
        handler.post(pollingRunnable)

        return view
    }

    private fun fetchRute(
        tvEstimasi: TextView,
        tvJarak: TextView,
        tvHalteCount: TextView
    ) {
        if (busName.isBlank()) return

        RetrofitClient.instance.getRute(busName)
            .enqueue(object : Callback<RuteResponse> {
                override fun onResponse(
                    call: Call<RuteResponse>,
                    response: Response<RuteResponse>
                ) {
                    if (!isAdded) return
                    val body = response.body() ?: return
                    if (!body.success) return

                    // Update header info
                    tvEstimasi.text   = body.estimasi
                    tvJarak.text      = body.jarak
                    tvHalteCount.text = "${body.sisaHalte}/${body.jumlahHalte}"

                    // Update halte list dengan warna dinamis
                    // Aturan: urutan < halteSekarang → sudah_lewat (tetap biru)
                    //         urutan == halteSekarang → sekarang (biru + pulse)
                    //         urutan > halteSekarang  → menuju (abu)
                    //
                    // "lewatSet" menyimpan urutan yang sudah pernah berstatus biru
                    // agar tidak bisa balik ke abu jika API lag/error sesaat

                    body.halte.forEach { halte ->
                        val status = halte.status
                        if (status == "sudah_lewat" || status == "sekarang") {
                            lewatSet.add(halte.urutan)
                        }
                    }

                    // Buat ulang list dengan status yang sudah diperkaya lewatSet
                    val enriched = body.halte.map { halte ->
                        val finalStatus = when {
                            halte.status == "tidak_aktif"            -> "tidak_aktif"
                            lewatSet.contains(halte.urutan)
                                    && halte.status != "sekarang"        -> "sudah_lewat"
                            else                                     -> halte.status
                        }
                        halte.copy(status = finalStatus)
                    }

                    adapter.updateData(enriched)
                }

                override fun onFailure(call: Call<RuteResponse>, t: Throwable) {
                    // Gagal fetch — tampilan terakhir tetap ada
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HalteAdapter.kt — letakkan di file terpisah atau di sini sebagai inner class
// ─────────────────────────────────────────────────────────────────────────────

class HalteAdapter(
    private var items: List<HalteItem>
) : RecyclerView.Adapter<HalteAdapter.VH>() {

    // Warna: biru = #1565C0, abu = #9E9E9E, teks = putih / gelap
    private val BLUE   = Color.parseColor("#1565C0")
    private val GRAY   = Color.parseColor("#9E9E9E")
    private val ACTIVE = Color.parseColor("#0D47A1") // biru lebih gelap = sekarang

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama    : TextView = view.findViewById(R.id.tvNamaHalte)
        val tvUrutan  : TextView = view.findViewById(R.id.tvUrutanHalte)
        val indicator : View     = view.findViewById(R.id.viewIndicator)
        val connector : View     = view.findViewById(R.id.viewConnector)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_halte, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val halte = items[position]

        holder.tvNama.text   = halte.namaHalte
        holder.tvUrutan.text = "${halte.urutan}"

        // Warna berdasarkan status — biru tidak bisa kembali ke abu
        when (halte.status) {
            "sekarang"    -> {
                holder.indicator.setBackgroundColor(ACTIVE)
                holder.tvNama.setTextColor(ACTIVE)
                holder.tvUrutan.setTextColor(ACTIVE)
                // Pulse animation (opsional)
                holder.indicator.animate().scaleX(1.15f).scaleY(1.15f)
                    .setDuration(600).withEndAction {
                        holder.indicator.animate().scaleX(1f).scaleY(1f).setDuration(600).start()
                    }.start()
            }
            "sudah_lewat" -> {
                holder.indicator.setBackgroundColor(BLUE)
                holder.tvNama.setTextColor(BLUE)
                holder.tvUrutan.setTextColor(BLUE)
            }
            else          -> {
                // "menuju" atau "tidak_aktif"
                holder.indicator.setBackgroundColor(GRAY)
                holder.tvNama.setTextColor(GRAY)
                holder.tvUrutan.setTextColor(GRAY)
            }
        }

        // Connector line (garis vertikal antara halte)
        // Sembunyikan connector di item terakhir
        holder.connector.visibility = if (position == items.size - 1) View.INVISIBLE else View.VISIBLE
        holder.connector.setBackgroundColor(
            if (halte.status == "sudah_lewat" || halte.status == "sekarang") BLUE else GRAY
        )
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<HalteItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}