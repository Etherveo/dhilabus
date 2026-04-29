package com.example.picbus

data class InfoRute(
    val estimasi: Int,
    val jarak: Double,
    val jumlahHalte: Int,
    val daftarHalte: List<String>
)

object DataRuteBus {
    val ruteBusA = InfoRute(40, 12.0, 9, listOf("Rektorat UNG)", "FIP", "Gerbang Kampus 1", "Gerbang Kampus 4", "Perpustakaan", "FSB", "FAPERTA", "FAKULTAS TEKNIK","FMIPA"))
    val ruteBusB = InfoRute(40, 12.0, 9, listOf("Rektorat UNG)", "FIP", "Gerbang Kampus 1", "Gerbang Kampus 4", "Perpustakaan", "FSB", "FAPERTA", "FAKULTAS TEKNIK","FMIPA"))
    val ruteBusC = InfoRute(40, 12.0, 9, listOf("FMIPA", "FAKULTAS TEKNIK", "FAPERTA", "FSB", "Perpustakaan", "Gerbang Kampus 4", "Gerbang Kampus 1", "FIP", "Rektorat UNG"))
    val ruteBusD = InfoRute(40, 12.0, 9, listOf("FMIPA", "FAKULTAS TEKNIK", "FAPERTA", "FSB", "Perpustakaan", "Gerbang Kampus 4", "Gerbang Kampus 1", "FIP", "Rektorat UNG"))

    fun getInfoRute(busName: String): InfoRute? {
        return when (busName.uppercase()) {
            "BUS A" -> ruteBusA
            "BUS B" -> ruteBusB
            "BUS C" -> ruteBusC
            "BUS D" -> ruteBusD
            else -> null
        }
    }
}