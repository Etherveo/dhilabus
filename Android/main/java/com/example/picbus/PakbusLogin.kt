package com.example.picbus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PakbusLogin : AppCompatActivity() {

    private lateinit var etKodePakbus: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_login_pakbus)

        etKodePakbus = findViewById(R.id.etUsernamePakbus)
        etPassword   = findViewById(R.id.etPasswordPakbus)
        btnLogin     = findViewById(R.id.btnLoginPakbus)

        btnLogin.setOnClickListener {
            val kode     = etKodePakbus.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (kode.isEmpty()) {
                etKodePakbus.error = "Kode Pakbus tidak boleh kosong"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            doLoginPakbus(kode, password)
        }
    }

    private fun doLoginPakbus(kode: String, password: String) {
        btnLogin.isEnabled = false
        btnLogin.text      = "Memproses..."

        RetrofitClient.instance.loginPakbus(kode, password)
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    btnLogin.isEnabled = true
                    btnLogin.text      = "Masuk sebagai Pakbus"

                    val body = response.body()

                    if (response.isSuccessful && body?.success == true) {

                        val token = body.access_token ?: ""

                        // Simpan semua data ke SharedPreferences
                        val sharedPref = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
                        sharedPref.edit()
                            .putString("accsess_token", token)
                            .putString("role", "pakbus")
                            .putString("userName", body.data?.name ?: kode)
                            .putString("userNim", body.data?.kode_pakbus ?: kode)
                            .putBoolean("isLocationActive", false)
                            .apply()

                        // Set token ke RetrofitClient SEKARANG
                        RetrofitClient.authToken = token

                        startActivity(
                            Intent(this@PakbusLogin, BerandaPakbus::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        )
                        finish()

                    } else {
                        Toast.makeText(
                            this@PakbusLogin,
                            body?.message ?: "Kode atau password salah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    btnLogin.isEnabled = true
                    btnLogin.text      = "Masuk sebagai Pakbus"
                    Toast.makeText(
                        this@PakbusLogin,
                        "Gagal terhubung: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}