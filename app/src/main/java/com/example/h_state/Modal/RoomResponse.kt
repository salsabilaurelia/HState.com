package com.example.h_state.Modal

import com.google.gson.annotations.SerializedName

data class RoomResponse(
    @SerializedName("rooms") val rooms: List<RoomHome>
)