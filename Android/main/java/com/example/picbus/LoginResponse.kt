package com.example.picbus

data class LoginResponse(
    val success: Boolean,
    val access_token: String?,
    val token_type: String?,
    val data: UserData?,
    val message: String?,
    val role: String?,
    val kode_pakbus: String?,
)


data class UserData(
    val id: Int,
    val name: String,
    val nim: String,
    val noTelp: String?,
    val kode_pakbus: String?,
    val role: String?
)
