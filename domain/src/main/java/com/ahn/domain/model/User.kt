package com.ahn.domain.model

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
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
