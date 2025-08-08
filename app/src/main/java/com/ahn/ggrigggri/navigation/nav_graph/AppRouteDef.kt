package com.ahn.ggrigggri.navigation.nav_graph

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import com.ahn.common_ui.R
import kotlinx.serialization.Serializable
import java.io.Serial

data class TopBarData(
    @StringRes var title: Int = 0,
    var titleRightIcon: ImageVector? = null,
    var titleLeftIcon: ImageVector? = null,
)

// AutoMirrored.Filled.ArrowBack
val NavBackStackEntry.topBarAsRouteName: TopBarData
    get() {
        val routeName = destination.route ?: return TopBarData()
        return when {
            routeName.contains("HomeTab") == true -> {
                TopBarData(
                    title = R.string.home_tab,
                    titleRightIcon = Icons.Outlined.Notifications
                )
            }

            routeName.contains("MemoryTab") == true -> {
                TopBarData(
                    title = R.string.memory_tab,
                    titleRightIcon = Icons.Outlined.Notifications
                )
            }

            routeName.contains("MyPageTab") == true -> {
                TopBarData(
                    title = R.string.myPage_tab,
                    titleRightIcon = Icons.Outlined.Notifications
                )
            }

            routeName.contains("PasswordReset") == true -> {
                TopBarData(
                    title = R.string.reset_password_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("SettingGroup") == true -> {
                TopBarData(
                    title = R.string.setting_group_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("ModifyUserPw") == true -> {
                TopBarData(
                    title = R.string.modify_user_pw_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("ModifyGroupPw") == true -> {
                TopBarData(
                    title = R.string.modify_group_pw_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("ModifyGroupName") == true -> {
                TopBarData(
                    title = R.string.modify_group_name_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("QuestionAnswer") == true -> {
                TopBarData(
                    title = R.string.archive_question_answer_title,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("Group") == true -> {
                TopBarData(
                    title = R.string.app_name,
                    titleLeftIcon = Icons.Outlined.ArrowBackIosNew
                )
            }

            routeName.contains("Login") == true -> {
                TopBarData()
            }

            else -> throw IllegalArgumentException("???")
        }
    }

@Serializable
sealed interface MainNavigationRoute {
    /**
    //     * 인자가 없는 화면이라면 object로 선언 인자가 있다면 dataClass
    //     */

    @Serializable
    data object HomeTab : MainNavigationRoute

    @Serializable
    data object MemoryTab : MainNavigationRoute

    @Serializable
    data object MyPageTab : MainNavigationRoute

    @Serializable
    data object PasswordReset : MainNavigationRoute

    @Serializable
    data object SettingGroup : MainNavigationRoute
    @Serializable
    data object ModifyUserPw : MainNavigationRoute

    @Serializable
    data object ModifyGroupPw : MainNavigationRoute

    @Serializable
    data object ModifyGroupName : MainNavigationRoute

    @Serializable
    data object QuestionAnswer : MainNavigationRoute

    @Serializable
    data object Login : MainNavigationRoute

    @Serializable
    data object Group: MainNavigationRoute
}

