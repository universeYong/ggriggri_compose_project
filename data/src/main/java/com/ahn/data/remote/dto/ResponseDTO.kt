package com.ahn.data.remote.dto

import com.google.firebase.firestore.DocumentId

data class ResponseDTO(
    @DocumentId
    val _responseId: String = "",
    val _responseMessage: String = "",
    val _responseCreatedTime: Long = 0L,
    val _responseImage: String = "",
    val _responseUserId: String = "",
    val _responseUserProfileImage: String = ""
)
