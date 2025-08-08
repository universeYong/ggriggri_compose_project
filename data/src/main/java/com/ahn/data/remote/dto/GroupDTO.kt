package com.ahn.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class GroupDTO(
    var _groupState: Int = 1, // 1: 활성화, 2: 비활성화 (기본값: 1)
    var _groupName: String = "",
    var _groupCode: String = "",
    var _groupPw: String = "",
    var _groupUserDocumentID: List<String> = listOf(),
    var _groupRequestDocumentID: List<String> = listOf(),
    var _groupQuestionDocumentID: List<String> = listOf(),
)