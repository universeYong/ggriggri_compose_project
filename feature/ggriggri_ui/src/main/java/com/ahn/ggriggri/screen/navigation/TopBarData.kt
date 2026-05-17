package com.ahn.ggriggri.screen.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import com.ahn.common_ui.R

data class TopBarData(
    @StringRes var title: Int = 0,
    var titleRightIcon: ImageVector? = null,
    var titleLeftIcon: ImageVector? = null,
    var IconOnClick: () -> Unit = {},
    var rightIconOnClick: () -> Unit = {}
)

// AutoMirrored.Filled.ArrowBack
val NavBackStackEntry.topBarAsRouteName: TopBarData
    get() {
        val routeName = destination.route ?: return TopBarData()

        return when {
            routeName.contains("HomeTab") -> {
                TopBarData(
                    title = R.string.home_tab,
                    titleRightIcon = Icons.Outlined.Notifications
                )
            }

            routeName.contains("MemoryTab") -> {
                TopBarData(
                    title = R.string.memory_tab,
                    titleRightIcon = Icons.Outlined.Notifications
                )
            }

            routeName.contains("MyPageTab") -> {
                TopBarData(
                    title = R.string.myPage_tab,
                    titleRightIcon = Icons.Outlined.Notifications
                )
            }

            routeName.contains("Answer") -> {
                TopBarData(
                    title = R.string.answer_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("PasswordReset") == true -> {
                TopBarData(
                    title = R.string.reset_password_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("SettingGroup") -> {
                TopBarData(
                    title = R.string.setting_group_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("ModifyUserPw") -> {
                TopBarData(
                    title = R.string.modify_user_pw_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("ModifyGroupPw") -> {
                TopBarData(
                    title = R.string.modify_group_pw_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("ModifyGroupName") -> {
                TopBarData(
                    title = R.string.modify_group_name_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("QuestionAnswer") -> {
                TopBarData(
                    title = R.string.archive_question_answer_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew,
                )
            }

            routeName.contains("Group") -> {
                TopBarData(
                    title = R.string.app_name,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("Login") -> {
                TopBarData()
            }

            routeName.contains("Request") -> {
                TopBarData(
                    title = R.string.request_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("Response") -> {
                TopBarData(
                    title = R.string.response_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            else -> throw IllegalArgumentException("???")
        }
    }