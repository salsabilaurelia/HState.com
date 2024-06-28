package com.example.h_state.Modal

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: User?,
    @SerializedName("fullName") val fullName: String? // Tambahan ini untuk menangani kasus di mana fullName ada di level atas JSON
)

data class User(
    @SerializedName("fullname") val fullname: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("profil") val profil: String?
)