package com.ahn.data.rest

import com.ahn.data.remote.dto.ApiResponseDTO
import com.ahn.data.remote.dto.FCMTokenRequestDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @GET("androidNetwork/bearerAction.ahn")
    suspend fun sendKakaoAccessToken(
        @Header("Authorization") token: String
    ): Response<String>

    @POST("/api/fcm-token")
    suspend fun updateFCMToken(
        @Body request: FCMTokenRequestDTO
    ): Response<ApiResponseDTO>
}