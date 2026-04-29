package com.example.picbus

import com.google.gson.annotations.SerializedName

data class RuteResponse(
    val success: Boolean,
    val bus: String,
    @SerializedName("bus_aktif")          val busAktif: Boolean,
    @SerializedName("bus_latitude")       val busLatitude: Double?,
    @SerializedName("bus_longitude")      val busLongitude: Double?,
    val estimasi: String,
    val jarak: String,
    @SerializedName("jumlah_halte")       val jumlahHalte: Int,
    @SerializedName("sisa_halte")         val sisaHalte: Int,
    @SerializedName("halte_sekarang")     val halteSekarang: Int,
    @SerializedName("halte_sekarang_id")  val halteSekarangId: Int?,
    @SerializedName("jarak_ke_halte_m")   val jarakKeHalteM: Int,
    val halte: List<HalteItem>
)

data class HalteItem(
    val id: Int,
    val urutan: Int,
    @SerializedName("nama_halte") val namaHalte: String,
    val latitude: Double?,
    val longitude: Double?,
    val status: String  // "sudah_lewat" | "sekarang" | "menuju" | "tidak_aktif"
)