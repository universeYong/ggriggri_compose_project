package com.ahn.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class QuestionListDTO(
    val _color: String,
    val _content: String,
    val _imgUrl: String,
    val _number: Int
)