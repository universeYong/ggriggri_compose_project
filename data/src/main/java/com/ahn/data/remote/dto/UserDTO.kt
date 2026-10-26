package com.ahn.data.remote.dto

import com.google.firebase.firestore.IgnoreExtraProperties

// FCM 토큰(_userFcmToken 배열)은 일부러 이 DTO에 두지 않는다.
// 토큰은 FirestoreUserRepositoryImpl.updateFcmToken/removeFcmToken이 arrayUnion/arrayRemove로만 다루고,
// Cloud Functions가 직접 읽는다. 레거시 필드 _userFcmCode는 더 이상 쓰지 않으며 문서에 남아 있어도 무시된다.
@IgnoreExtraProperties //Firestore DB에는 있지만 data class 없는 필드는 무시
data class UserDTO(
    var _userId: String = "",
    var _userName: String = "",
    var _userState: Int = 1,
    var _userJoinTime: Long = 0L,
    var _userProfileImage: String = "",
    var _userGroupDocumentId: String = "",
    var _userAutoLoginToken: String = "",
)
