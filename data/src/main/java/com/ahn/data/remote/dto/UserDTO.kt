package com.ahn.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties //Firestore DB에는 있지만 data class 없는 필드는 무시
data class UserDTO(
    var _userId: String = "",
    var _userName: String = "",
    var _userState: Int = 1,
    var _userJoinTime: Long = 0L,
    var _userFcmCode: MutableList<String> = mutableListOf(),
    var _userProfileImage: String = "",
    var _userGroupDocumentId: String = "",
    var _userAutoLoginToken: String = "",
)