package com.ahn.data.mapper

import com.ahn.data.remote.dto.QuestionDTO
import com.ahn.data.remote.dto.QuestionListDTO
import com.ahn.domain.model.QuestionList

fun QuestionListDTO.toDomainQuestionList(): QuestionList = QuestionList(
    color = _color,
    content = _content,
    imgUrl = _imgUrl,
    number = _number
)

fun QuestionList.toFirestoreQuestionListDTO() = mapOf(
    "_color" to this.color,
    "_content" to this.content,
    "_imgUrl" to this.imgUrl,
    "_number" to this.number
)
