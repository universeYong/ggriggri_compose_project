package com.ahn.domain.model

data class Answer(
    var answerID: String = "",
    var answerMessage: String = "",
    var answerResponseTime: Long = 0L,
    var answerRequestState: Int = 1,
    var answerUserDocumentID: String = ""
)