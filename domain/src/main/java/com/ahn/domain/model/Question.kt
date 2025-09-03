package com.ahn.domain.model

data class Question(
    var questionId: String = "",
    var questionCreatedTime: Long = 0L,
    var questionGroupDocumentId: String = "",
    var questionListDocumentId: String = "",
)