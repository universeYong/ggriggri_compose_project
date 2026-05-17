package com.ahn.domain.model

data class Group(
    var groupDocumentId: String = "",
    var groupState: Int = 1, // 1: 활성화, 2: 비활성화 (기본값: 1)
    var groupName: String = "",
    var groupCode: String = "",
    var groupPw: String = "",
    var groupUserDocumentID: List<String> = listOf(),
    var groupRequestDocumentID: List<String> = listOf(),
    var groupQuestionDocumentID: List<String> = listOf(),
)