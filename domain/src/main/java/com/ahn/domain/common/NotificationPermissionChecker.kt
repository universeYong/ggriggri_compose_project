package com.ahn.domain.common

/**
 * 알림 권한 상태 조회용 계약.
 * 실제 권한 요청은 Activity가 필요하므로 화면(Composable)에서 ActivityResult API로 처리한다.
 */
interface NotificationPermissionChecker {
    fun hasNotificationPermission(): Boolean
    fun shouldRequestNotificationPermission(): Boolean
}
