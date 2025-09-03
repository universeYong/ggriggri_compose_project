package com.ahn.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class QuestionDTO(
    var _questionId: String = "",
    var _questionCreatedTime: Long = 0L,
    var _questionGroupDocumentId: String = "",
    var _questionListDocumentId: String = "",
)