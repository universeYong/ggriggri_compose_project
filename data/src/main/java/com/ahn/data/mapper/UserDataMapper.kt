package com.ahn.data.mapper

import com.ahn.domain.model.User
import com.ahn.data.remote.dto.UserDTO


// User.userFcmCode는 앱 어디에서도 읽지 않는 레거시 필드다.
// 서버 토큰(_userFcmToken)을 도메인으로 끌어오지 않고 항상 빈 목록으로 둔다
// (다른 사용자의 토큰이 세션 JSON 등에 섞여 들어가지 않게 하기 위함).
// userAutoLoginToken(카카오 access token)은 서버에 저장하지 않으므로 항상 빈 값이다.
// 세션 토큰은 SessionManager(로컬 DataStore)만 보관하며, 서버에서 읽은 User는 SessionManager.updateUser로 반영한다.
fun UserDTO.toDomainUser() = User(
    userId = this._userId,
    userName = this._userName,
    userState = this._userState,
    userJoinTime = this._userJoinTime,
    userFcmCode = mutableListOf(),
    userProfileImage = this._userProfileImage,
    userGroupDocumentId = this._userGroupDocumentId,
)

// create()(set merge)와 update()가 이 맵을 그대로 쓴다.
// 토큰 필드(_userFcmToken, 레거시 _userFcmCode)는 넣지 않는다. 넣으면 update() 때
// 다른 기기에서 arrayUnion으로 추가한 토큰 배열을 덮어써 푸시가 끊긴다.
// 카카오 access token(_userAutoLoginToken)도 다른 그룹원이 읽을 수 있는 문서에 평문으로 남지 않도록 넣지 않는다.
// (기존 문서에 남아 있는 _userAutoLoginToken 값은 merge/update가 지우지 않으므로 별도로 정리해야 한다.)
fun User.toFirestoreUserDTO() = mapOf(
    "_userId" to this.userId,
    "_userName" to this.userName,
    "_userState" to this.userState,
    "_userJoinTime" to this.userJoinTime,
    "_userProfileImage" to this.userProfileImage,
    "_userGroupDocumentId" to this.userGroupDocumentId,
)

fun List<UserDTO>.toDomainUserList() =
    this.map {it.toDomainUser()}.toList()
