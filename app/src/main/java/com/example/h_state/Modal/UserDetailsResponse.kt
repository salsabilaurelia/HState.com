package com.example.h_state.Modal

import org.json.JSONObject

data class UserDetailsResponse(
    val rawResponse: String
) {
    fun parseJson(): Pair<String, String>? {
        return try {
            val jsonString = rawResponse.substringAfter("{")
            val cleanedJson = "{$jsonString"
            val jsonObject = JSONObject(cleanedJson)
            Pair(jsonObject.getString("status"), jsonObject.getString("fullName"))
        } catch (e: Exception) {
            null
        }
    }
}