package com.ahn.ggriggri.auth

import com.ahn.domain.common.KakaoTokenProvider
import com.kakao.sdk.auth.AuthApiClient
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 카카오 SDK가 기기에 보관한 토큰(refresh token 포함)으로 access token을 갱신해 돌려준다.
 * 갱신된 토큰은 SDK가 스스로 저장한다. 토큰은 로그에 남기지 않는다.
 */
@Singleton
class KakaoSdkTokenProvider @Inject constructor() : KakaoTokenProvider {

    override suspend fun refreshAccessToken(): String? = suspendCancellableCoroutine { cont ->
        if (!AuthApiClient.instance.hasToken()) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        AuthApiClient.instance.refreshAccessToken { token, error ->
            if (cont.isActive) cont.resume(if (error == null) token?.accessToken else null)
        }
    }
}
