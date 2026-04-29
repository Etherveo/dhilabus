package com.example.picbus

data class RegisterResponse(
    val success: Boolean,
    val access_token: String?,
    val token_type: String?,
    val data: UserData?,
    val message: String?
)
