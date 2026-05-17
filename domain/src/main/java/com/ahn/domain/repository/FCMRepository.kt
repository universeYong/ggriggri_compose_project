package com.ahn.domain.repository

interface FCMRepository {
    suspend fun getCurrentToken(): String?
    suspend fun refreshToken()
    suspend fun saveTokenToServer(token: String)
}