package com.example.picbus

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("api/login")
    fun loginUser(
        @Field("nim") nim: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("api/pakbus/login")
    fun loginPakbus(
        @Field("kode_pakbus") kodePakbus: String,
        @Field("password") password: String,
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("api/register")
    fun registerUser(
        @Field("name") name: String,
        @Field("nim") nim: String,
        @Field("noTelp") noTelp: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("api/pakbus/update-location")
    fun updateLocation(
        @Field("kode_pakbus") kodePakbus: String,
        @Field("location_link") locationLink: String
    ): Call<BusResponse>

    @GET("api/bus/semua-posisi")
    fun semuaPosisiBus(): Call<BusPositionResponse>

    // Untuk pakbus: update koordinat GPS
    @FormUrlEncoded
    @POST("api/pakbus/update-koordinat")
    fun updateKoordinat(
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double
    ): Call<UpdateKoordinatResponse>

    @FormUrlEncoded
    @POST("api/pakbus/toggle-aktif")
    fun toggleAktif(
        @Field("status") status: Int
    ): Call<ToggleAktifResponse>

    @GET("api/bus/posisi-halte")
    fun posisiPerHalte(): Call<BusPosisiHalteResponse>

    @GET("api/rute/{busName}")
    fun getRute(
        @retrofit2.http.Path("busName") busName: String
    ): Call<RuteResponse>


}
