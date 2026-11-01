package com.ahn.data.repository

import android.util.Log
import com.ahn.domain.repository.AuthRepository
import com.ahn.domain.repository.KakaoTokenRejectedException
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.auth
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.functions
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * 카카오 access token -> (callable `kakaoCustomToken`) -> Firebase custom token -> Firebase Auth 로그인.
 * 서버 계약: 요청 { accessToken }, 응답 { firebaseToken }, 리전 us-central1. uid는 카카오 회원번호 문자열.
 * 토큰과 사용자 ID는 로그에 남기지 않는다.
 */
@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor() : AuthRepository {

    private val auth get() = Firebase.auth
    private val functions get() = Firebase.functions(FUNCTIONS_REGION)

    override suspend fun signInWithKakaoToken(kakaoAccessToken: String): Result<Unit> = try {
        val response = functions
            .getHttpsCallable(CALLABLE_NAME)
            .call(mapOf("accessToken" to kakaoAccessToken))
            .await()
        val firebaseToken = (response.getData() as? Map<*, *>)?.get("firebaseToken") as? String
        if (firebaseToken.isNullOrBlank()) {
            throw IllegalStateException("서버 응답에 firebaseToken이 없습니다.")
        }
        auth.signInWithCustomToken(firebaseToken).await()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: FirebaseFunctionsException) {
        Log.e(TAG, "토큰 교환 실패: code=${e.code}")
        // 서버가 카카오 토큰을 검증하지 못한 경우(UNAUTHENTICATED 등)는 재로그인이 필요한 실패로 구분한다.
        val rejected = e.code == FirebaseFunctionsException.Code.UNAUTHENTICATED ||
            e.code == FirebaseFunctionsException.Code.PERMISSION_DENIED ||
            e.code == FirebaseFunctionsException.Code.INVALID_ARGUMENT
        Result.failure(if (rejected) KakaoTokenRejectedException(e) else e)
    } catch (e: FirebaseNetworkException) {
        Log.e(TAG, "Firebase 로그인 네트워크 오류")
        Result.failure(e)
    } catch (e: Exception) {
        Log.e(TAG, "Firebase 로그인 실패: ${e.javaClass.simpleName}")
        Result.failure(e)
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun isSignedIn(): Boolean = auth.currentUser != null

    private companion object {
        const val TAG = "FirebaseAuthRepository"
        const val FUNCTIONS_REGION = "us-central1"
        const val CALLABLE_NAME = "kakaoCustomToken"
    }
}
