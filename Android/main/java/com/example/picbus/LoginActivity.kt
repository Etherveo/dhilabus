package com.example.picbus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var nimEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var tvToRegister: TextView
    private lateinit var tvToPakbus: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_login)

        nimEditText = findViewById(R.id.nim)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.btnLogin)
        tvToRegister = findViewById(R.id.toregister)
        tvToPakbus = findViewById(R.id.toPakbusLogin)

        tvToRegister.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        tvToPakbus.setOnClickListener {
            val intent = Intent(this, PakbusLogin::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val inputNim = nimEditText.text.toString().trim()
            val inputPassword = passwordEditText.text.toString().trim()

            if (inputNim.isNotEmpty() && inputPassword.isNotEmpty()) {
                prosesLogin(inputNim, inputPassword)
            } else {
                Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prosesLogin(nim: String, password: String) {
        RetrofitClient.instance.loginUser(nim, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        // SIMPAN NAMA KE SHARED PREFERENCES
                        val sharedPref = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("userName", body.data?.name)
                        editor.putString("userNim", body.data?.nim)
                        editor.putString("accsess_token", body.access_token)
                        editor.apply()


                        Toast.makeText(this@LoginActivity, "Selamat Datang!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Gagal: ${body?.message ?: "Response tidak valid"}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login gagal (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message ?: "Tidak bisa terhubung ke server"}", Toast.LENGTH_LONG).show()
            }
        })
    }
}