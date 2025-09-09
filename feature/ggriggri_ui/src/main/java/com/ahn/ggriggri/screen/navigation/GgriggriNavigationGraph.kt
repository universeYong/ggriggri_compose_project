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
import com.ahn.ggriggri.screen.auth.resetpw.ResetPwScreen
import com.ahn.ggriggri.screen.group.GroupScreen
import com.ahn.ggriggri.screen.main.answer.AnswerScreen
import com.ahn.ggriggri.screen.main.home.HomeScreen
import com.ahn.ggriggri.screen.setting.modifygroupname.ModifyGroupNameScreen
import com.ahn.ggriggri.screen.setting.modifygrouppw.ModifyGroupPwScreen
import com.ahn.ggriggri.screen.setting.mypage.MyPageScreen
import com.ahn.ggriggri.screen.setting.settinggroup.SettingGroupScreen
import com.ahn.ggriggri.screen.ui.auth.viewmodel.OAuthViewModel

data class MainFactories(
    val home: ViewModelProvider.Factory,
    val answer: ViewModelProvider.Factory,
    val archive: ViewModelProvider.Factory,
    val questionAnswer: ViewModelProvider.Factory,
    val myPage: ViewModelProvider.Factory,
    val auth: ViewModelProvider.Factory
)

fun NavGraphBuilder.GgriggriNavigationGraph(
    navController: NavController,
    factories: MainFactories
) {

    composable<GgriggriNavigationRouteUi.MemoryTab> {
        MemoryScreen(
            archiveViewModel = viewModel(factory = factories.archive),
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
        HomeScreen(homeViewmodel = viewModel(factory = factories.home),
            onNavigationToAnswer = {navController.navigate(GgriggriNavigationRouteUi.Answer)})
    }
    composable<GgriggriNavigationRouteUi.MyPageTab> {
        MyPageScreen(
            myPageviewModel = viewModel(factory = factories.myPage)
        )
    }

    /********* archive ******************************************************/
    composable<GgriggriNavigationRouteUi.QuestionAnswer> {
        QuestionAnswerScreen(
            questionAnswerViewModel = viewModel(factory = factories.questionAnswer),
        )
    }
    composable<GgriggriNavigationRouteUi.QuestionList>{
        QuestionListScreen(
            archiveViewModel = viewModel(factory = factories.archive),
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
    composable<GgriggriNavigationRouteUi.PasswordReset> {
        ResetPwScreen()
    }

    composable<GgriggriNavigationRouteUi.Login> {
        LoginScreen(
            authViewModel = viewModel(factory = factories.auth),
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
        GroupScreen(authViewModel = viewModel(factory = factories.auth))
    }

    /********* main *********************************************************/
    composable <GgriggriNavigationRouteUi.Answer> {
        AnswerScreen(
            answerViewModel = viewModel(factory = factories.answer),
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