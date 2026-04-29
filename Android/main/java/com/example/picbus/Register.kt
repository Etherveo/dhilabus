package com.example.picbus

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Register : AppCompatActivity() {

    private lateinit var nama: EditText
    private lateinit var nim: EditText
    private lateinit var noTelp: EditText
    private lateinit var password: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_register)

        // Inisialisasi View
        nama = findViewById(R.id.nama)
        nim = findViewById(R.id.nim)
        noTelp = findViewById(R.id.no_telp)
        password = findViewById(R.id.password)
        btnRegister = findViewById(R.id.btnregister)

        btnRegister.setOnClickListener {
            val namaStr = nama.text.toString().trim()
            val nimStr = nim.text.toString().trim()
            val noTelpStr = noTelp.text.toString().trim()
            val passwordStr = password.text.toString().trim()

            if (namaStr.isNotEmpty() && nimStr.isNotEmpty() && noTelpStr.isNotEmpty() && passwordStr.isNotEmpty()) {
                prosesRegister(namaStr, nimStr, noTelpStr, passwordStr)
            } else {
                Toast.makeText(this, "Harap isi semua kolom!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prosesRegister(nama: String, nim: String, noTelp: String, pass: String) {
        // Memanggil registerUser dengan parameter terpisah sesuai ApiService.kt
        RetrofitClient.instance.registerUser(nama, nim, noTelp, pass).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.success == true) {
                        Toast.makeText(this@Register, "Registrasi Berhasil! Silakan Login", Toast.LENGTH_LONG).show()
                        // Setelah daftar, arahkan balik ke halaman Login (MainActivity)
                        finish()
                    } else {
                        Toast.makeText(this@Register, "Gagal: ${res?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@Register, "Register gagal (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@Register, "Koneksi Bermasalah: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}