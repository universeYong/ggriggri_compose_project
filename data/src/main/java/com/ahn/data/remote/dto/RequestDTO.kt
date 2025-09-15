package com.ahn.data.remote.dto

data class RequestDTO(
    var _requestId: String = "",
    val _requestTime: Long = 0L,
    val _requestUserDocumentID: String = "",
    val _requestMessage: String = "",
    val _requestImage: String = "",
    val _requestGroupDocumentID: String = "",
    val _answerDeadline: Long = 0L,
    val _hasAnswer: Boolean = false
)