package com.ahn.data.mapper

import com.ahn.data.remote.dto.ResponseDTO
import com.ahn.domain.model.Response

fun ResponseDTO.toDomainResponse(documentId: String): Response = Response(
    responseId = documentId,
    responseTime = _responseCreatedTime,
    responseImage = _responseImage,
    responseMessage = _responseMessage,
    responseUserDocumentID = _responseUserId,
    responseUserProfileImage = _responseUserProfileImage
)

fun Response.toFirestoreResponseDTO() = mapOf(
    "_responseMessage" to this.responseMessage,
    "_responseCreatedTime" to this.responseTime,
    "_responseImage" to this.responseImage,
    "_responseUserId" to this.responseUserDocumentID,
    "_responseUserProfileImage" to this.responseUserProfileImage,
)