package com.ahn.domain.model

// :data의 SessionManagerImpl이 Moshi KotlinJsonAdapterFactory(리플렉션)로 직렬화한다.
// domain은 Moshi에 의존하지 않으므로 @JsonClass를 붙이지 않는다(프로퍼티 이름이 그대로 JSON 키가 됨).
data class User(
    var userId: String = "",
    var userName: String = "",
    var userState: Int = 1,
    var userJoinTime: Long = 0L,
    var userFcmCode: MutableList<String> = mutableListOf(),
    var userProfileImage: String = "",
    var userGroupDocumentId: String = "",
    var userAutoLoginToken: String = "",
)
