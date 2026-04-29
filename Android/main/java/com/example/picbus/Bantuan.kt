package com.example.picbus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class Bantuan : Fragment() {

    private var phone1: String? = null
    private var phone2: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bantuan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvBus = view.findViewById<TextView>(R.id.tvNamaBus)
        val etSaran = view.findViewById<EditText>(R.id.etSaran)
        val btnKirimSaran = view.findViewById<Button>(R.id.btnKirimSaran)

        // Kontak 1
        val card1 = view.findViewById<CardView>(R.id.cardKontak1)
        val tvNama1 = view.findViewById<TextView>(R.id.tvNamaKondektur1)
        val tvTelp1 = view.findViewById<TextView>(R.id.tvNomorTelp1)
        val btnHubungi1 = view.findViewById<Button>(R.id.btnHubungi1)

        // Kontak 2
        val card2 = view.findViewById<CardView>(R.id.cardKontak2)
        val tvNama2 = view.findViewById<TextView>(R.id.tvNamaKondektur2)
        val tvTelp2 = view.findViewById<TextView>(R.id.tvNomorTelp2)
        val btnHubungi2 = view.findViewById<Button>(R.id.btnHubungi2)

        val busName = arguments?.getString("bus_type")

        if (busName != null) {
            tvBus.text = "Bus yang dipilih: $busName"
            setupContacts(busName, card1, tvNama1, tvTelp1, card2, tvNama2, tvTelp2)
        } else {
            tvBus.text = "Silakan pilih bus pada jadwal"
            card1.visibility = View.GONE
            card2.visibility = View.GONE
        }

        btnHubungi1.setOnClickListener {
            if (tvNama1.text.toString().contains("(Whatsapp)", ignoreCase = true)) {
                openWhatsApp(phone1)
            } else {
                dialPhone(phone1)
            }
        }

        btnHubungi2.setOnClickListener {
            if (tvNama2.text.toString().contains("(Whatsapp)", ignoreCase = true)) {
                openWhatsApp(phone2)
            } else {
                dialPhone(phone2)
            }
        }

        btnKirimSaran.setOnClickListener {
            val saran = etSaran.text.toString().trim()
            if (saran.isNotEmpty()) {
                Toast.makeText(context, "Saran Anda telah terkirim. Terima kasih!", Toast.LENGTH_LONG).show()
                etSaran.text.clear()
            }
        }
    }

    private fun setupContacts(bus: String, c1: CardView, n1: TextView, t1: TextView, c2: CardView, n2: TextView, t2: TextView) {
        val busUpper = bus.uppercase()
        
        // Reset Visibility
        c1.visibility = View.VISIBLE
        c2.visibility = View.GONE
        phone1 = null
        phone2 = null

        when {
            busUpper.contains("BUS A") -> {
                n1.text = "Rian (Whatsapp)"
                phone1 = "089530911565"
                t1.text = phone1
            }
            busUpper.contains("BUS B") -> {
                n1.text = "Ilham (Whatsapp)"
                phone1 = "085241111580"
                t1.text = phone1

                c2.visibility = View.VISIBLE
                n2.text = "Pak Tamim"
                phone2 = "083840851853"
                t2.text = phone2
            }
            busUpper.contains("BUS C") -> {
                n1.text = "Arif"
                phone1 = "082291321073"
                t1.text = phone1

                c2.visibility = View.VISIBLE
                n2.text = "Pak Saiful"
                phone2 = "085244164336"
                t2.text = phone2
            }
            busUpper.contains("BUS D") -> {
                n1.text = "Syahril"
                phone1 = "085216013639"
                t1.text = phone1
                
                c2.visibility = View.VISIBLE
                n2.text = "Pak Karim (Whatsapp)"
                phone2 = "082348079156"
                t2.text = phone2
            }
        }
    }

    private fun dialPhone(phone: String?) {
        if (phone != null) {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone")
            startActivity(intent)
        } else {
            Toast.makeText(context, "Kontak tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWhatsApp(phone: String?) {
        if (phone != null) {
            // Format nomor ke format internasional (ganti 0 di depan dengan 62)
            var formattedNumber = phone.replace(" ", "").replace("-", "")
            if (formattedNumber.startsWith("0")) {
                formattedNumber = "62" + formattedNumber.substring(1)
            }
            
            try {
                val url = "https://api.whatsapp.com/send?phone=$formattedNumber"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp tidak terinstal", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Kontak tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }
}
