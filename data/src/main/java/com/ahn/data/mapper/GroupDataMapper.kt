package com.ahn.data.mapper

import com.ahn.data.remote.dto.GroupDTO
import com.ahn.domain.model.Group
import kotlin.Int

fun GroupDTO.toDomainGroup() = Group(
    _groupState,
    _groupName,
    _groupCode,
    _groupPw,
    _groupUserDocumentID,
    _groupRequestDocumentID,
    _groupQuestionDocumentID
)

fun Group.toFirestoreGroupDTO() = mapOf(
    "_groupState" to this.groupState,
    "_groupName" to this.groupName,
    "_groupCode" to this.groupCode,
    "_groupPw" to this.groupPw,
    "_groupUserDocumentID" to this.groupUserDocumentID,
    "_groupRequestDocumentID" to this.groupRequestDocumentID,
    "_groupQuestionDocumentID" to this.groupQuestionDocumentID
)
