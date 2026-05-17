package com.ahn.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class AnswerDTO(
    var _answerID: String = "",
    var _answerMessage: String = "",
    var _answerResponseTime: Long = 0L,
    var _answerRequestState: Int = 1,
    var _answerUserDocumentID: String = ""
)
