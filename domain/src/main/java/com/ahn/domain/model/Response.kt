package com.ahn.domain.model

data class Response(
    val responseId: String = "", // 응답 ID
    val responseTime: Long = 0L, // 응답 생성 시간
    val responseImage: String = "", // 응답 이미지 URL
    val responseMessage: String = "", // 응답 메시지
    val responseUserDocumentID: String = "", // 응답한 사용자 ID
    val responseUserProfileImage: String = "" // 응답한 사용자 프로필 이미지 URL
)
