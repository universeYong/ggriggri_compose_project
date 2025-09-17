package com.ahn.data.repository

import com.ahn.data.fcm.FCMTokenManager
import com.ahn.domain.repository.FCMRepository
import jakarta.inject.Inject

class FirebaseFCMRepositoryImpl @Inject constructor(
    private val fcmTokenManager: FCMTokenManager
) : FCMRepository {
    override suspend fun getCurrentToken(): String? {
        return fcmTokenManager.getCurrentToken()
    }

    override suspend fun refreshToken() {
        fcmTokenManager.refreshToken()
    }

    override suspend fun saveTokenToServer(token: String) {
        fcmTokenManager.saveTokenToServer(token)
    }

}