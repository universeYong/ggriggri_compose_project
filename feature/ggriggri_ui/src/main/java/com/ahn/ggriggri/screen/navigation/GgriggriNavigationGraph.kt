package com.ahn.ggriggri.screen.navigation

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ahn.ggriggri.screen.archive.memory.MemoryScreen
import com.ahn.ggriggri.screen.archive.questionanswer.QuestionAnswerScreen
import com.ahn.ggriggri.screen.archive.questionlist.QuestionListScreen
import com.ahn.ggriggri.screen.auth.login.LoginScreen
import com.ahn.ggriggri.screen.group.GroupScreen
import com.ahn.ggriggri.screen.main.answer.AnswerScreen
import com.ahn.ggriggri.screen.main.home.HomeScreen
import com.ahn.ggriggri.screen.setting.modifygroupname.ModifyGroupNameScreen
import com.ahn.ggriggri.screen.setting.modifygrouppw.ModifyGroupPwScreen
import com.ahn.ggriggri.screen.setting.mypage.MyPageScreen
import com.ahn.ggriggri.screen.setting.settinggroup.SettingGroupScreen

fun NavGraphBuilder.GgriggriNavigationGraph(
    navController: NavController,
) {

    composable<GgriggriNavigationRouteUi.MemoryTab> {
        MemoryScreen(
            onNavigateToQuestionAnswerActual = { questionId ->
                if (questionId.isNotBlank()) {
                    Log.d(
                        "EntryPointScreen",
                        "From MemoryTab: Attempting to navigate to QuestionAnswer with ID: $questionId"
                    )
                    navController.navigate(GgriggriNavigationRouteUi.QuestionAnswer(questionDataId = questionId))
                    Log.d(
                        "EntryPointScreen",
                        "From MemoryTab: navController.navigate called for QuestionAnswer."
                    )
                } else {
                    Log.e(
                        "EntryPointScreen",
                        "From MemoryTab: Attempted to navigate to QuestionAnswer with blank ID."
                    )
                }
            }
        )
    }
    composable<GgriggriNavigationRouteUi.HomeTab> {
        HomeScreen(
            onNavigationToAnswer = {navController.navigate(GgriggriNavigationRouteUi.Answer)})
    }
    composable<GgriggriNavigationRouteUi.MyPageTab> {
        MyPageScreen()
    }

    /********* archive ******************************************************/
    composable<GgriggriNavigationRouteUi.QuestionAnswer> {
        QuestionAnswerScreen()
    }
    composable<GgriggriNavigationRouteUi.QuestionList>{
        QuestionListScreen(
            onNavigateToQuestionAnswer = { questionId ->
                if (questionId.isNotBlank()) {
                    Log.d("EntryPointScreen", "Attempting to navigate to QuestionAnswer with ID: $questionId") // ★★★ 로그 추가 ★★★
                    navController.navigate(GgriggriNavigationRouteUi.QuestionAnswer(questionDataId = questionId))
                    Log.d("EntryPointScreen", "navController.navigate called for QuestionAnswer.") // ★★★ 로그 추가 ★★★
                } else {
                    Log.e(
                        "EntryPointScreen",
                        "Attempted to navigate to QuestionAnswer with blank ID."
                    )
                }
            }
        )
    }


    /********* auth *********************************************************/

    composable<GgriggriNavigationRouteUi.Login> {
        LoginScreen(
            onNavigationToGroup = {
                Log.d("AppNavigation", "onNavigationToGroup called") // 로그 추가
                navController.navigate(GgriggriNavigationRouteUi.Group)
            },
            onNavigationToHome = {
                Log.d("AppNavigation", "onNavigationToHome called") // 로그 추가
                navController.navigate(GgriggriNavigationRouteUi.HomeTab) {
                    popUpTo(GgriggriNavigationRouteUi.Login) {inclusive = true}
                }
            }
        )
    }

    /********* group ********************************************************/
    composable<GgriggriNavigationRouteUi.Group> {
        GroupScreen()
    }

    /********* main *********************************************************/
    composable <GgriggriNavigationRouteUi.Answer> {
        AnswerScreen(
            onNavigateBack = {navController.navigate(GgriggriNavigationRouteUi.HomeTab)}
        )
    }

    /********* setting ******************************************************/
    composable<GgriggriNavigationRouteUi.SettingGroup> {
        SettingGroupScreen()
    }
    composable<GgriggriNavigationRouteUi.ModifyGroupPw> {
        ModifyGroupPwScreen()
    }
    composable<GgriggriNavigationRouteUi.ModifyGroupName> {
        ModifyGroupNameScreen()
    }

}