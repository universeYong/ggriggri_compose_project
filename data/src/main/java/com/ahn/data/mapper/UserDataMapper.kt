package com.ahn.data.mapper

import com.ahn.domain.model.User
import com.ahn.data.remote.dto.UserDTO


fun UserDTO.toDomainUser() = User(
    userId = this._userId,
    userName = this._userName,
    userState = this._userState,
    userJoinTime = this._userJoinTime,
    userFcmToken = this._userFcmToken,
    userProfileImage = this._userProfileImage,
    userGroupDocumentId = this._userGroupDocumentId,
    userAutoLoginToken = this._userAutoLoginToken,
)

fun User.toFirestoreUserDTO() = mapOf(
    "_userId" to this.userId,
    "_userName" to this.userName,
    "_userState" to this.userState,
    "_userJoinTime" to this.userJoinTime,
    "_userFcmToken" to this.userFcmToken,
    "_userProfileImage" to this.userProfileImage,
    "_userGroupDocumentId" to this.userGroupDocumentId,
    "_userAutoLoginToken" to this.userAutoLoginToken,
)

fun List<UserDTO>.toDomainUserList() =
    this.map {it.toDomainUser()}.toList()