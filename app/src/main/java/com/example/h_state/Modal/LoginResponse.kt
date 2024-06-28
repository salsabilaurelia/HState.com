package com.example.h_state.Modal

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val response: Boolean,
    val message: String,
    val payload: UserPayload?
)

data class UserPayload(
    val email: String,
    val fullname: String
)



