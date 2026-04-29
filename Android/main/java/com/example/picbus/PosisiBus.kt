package com.example.picbus

import com.google.gson.annotations.SerializedName

// Response GET /api/bus/semua-posisi
data class BusPositionResponse(
    val success: Boolean,
    val buses: List<BusPosition>
)

data class BusPosition(
    @SerializedName("bus_id")   val busId: Int,
    @SerializedName("nama_bus") val namaBus: String,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("is_online") val isOnline: Boolean
)

// Response POST /api/pakbus/toggle-aktif
data class ToggleAktifResponse(
    val success: Boolean,
    val message: String,
    val status: Boolean
)

// Response POST /api/pakbus/update-koordinat
data class UpdateKoordinatResponse(
    val success: Boolean,
    val message: String
)

data class BusPosisiHalteResponse(
    val success: Boolean,
    val buses: List<BusPosisiHalte>
)

data class BusPosisiHalte(
    @SerializedName("bus_id")         val busId: Int,
    @SerializedName("nama_bus")       val namaBus: String,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("is_online")      val isOnline: Boolean,
    @SerializedName("halte_sekarang") val halteSekarang: Int?,
    val kondisi: String
)