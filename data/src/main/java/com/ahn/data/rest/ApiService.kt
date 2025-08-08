package com.ahn.data.rest

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("androidNetwork/bearerAction.ahn")
    suspend fun sendKakaoAccessToken(
        @Header("Autorizition") token: String
    ): Response<String>
}