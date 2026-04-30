// app/src/main/java/com/example/picbus/RetrofitClient.kt
package com.example.picbus

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http:/192.168.1.14:8000/"


    var authToken: String = ""


    private val okHttpClient: OkHttpClient by lazy {

        // Logging — tampilkan request/response di Logcat saat debug
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            // Interceptor token: setiap request otomatis dapat header Authorization
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")

                // Hanya tambahkan token kalau sudah ada isinya
                if (authToken.isNotEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $authToken")
                }

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)   // hapus baris ini kalau sudah production
            .build()
    }

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)      // ← pakai client yang sudah ada interceptor-nya
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}