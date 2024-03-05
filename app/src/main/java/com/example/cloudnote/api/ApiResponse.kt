package com.example.cloudnote.api

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

data class ApiResponse(val statusCode: Int, val json: String) {
    fun getError(): ApiError? {
        try {
            return Gson().fromJson(json, ApiError::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            return null
        }
    }
}
