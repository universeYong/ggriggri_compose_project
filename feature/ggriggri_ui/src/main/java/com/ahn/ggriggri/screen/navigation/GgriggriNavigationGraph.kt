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
import com.ahn.ggriggri.screen.archive.requestdetail.RequestDetailScreen
import com.ahn.ggriggri.screen.archive.requestlist.RequestListScreen
import com.ahn.ggriggri.screen.auth.login.LoginScreen
import com.ahn.ggriggri.screen.ui.auth.ui.devlogin.DevLoginScreen
import com.ahn.ggriggri.screen.group.GroupScreen
import com.ahn.ggriggri.screen.main.answer.AnswerScreen
import com.ahn.ggriggri.screen.main.home.HomeScreen
import com.ahn.ggriggri.screen.main.request.RequestScreen
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
                    navController.navigate(GgriggriNavigationRouteUi.QuestionAnswer(questionDataId = questionId))
                }
            },
            onNavigateToRequestDetailActual = { requestId ->
                if(requestId.isNotBlank()){
                    navController.navigate(GgriggriNavigationRouteUi.RequestDetail(requestId = requestId))
                }
            }
        )
    }
    composable<GgriggriNavigationRouteUi.HomeTab> {
        HomeScreen(
            onNavigationToAnswer = {navController.navigate(GgriggriNavigationRouteUi.Answer)},
            onNavigateToRequest = {navController.navigate(GgriggriNavigationRouteUi.Request)},
            onNavigateToResponse = {navController.navigate(GgriggriNavigationRouteUi.Response)},
            onNavigateToRequestDetail = { request -> 
                if (request.requestId.isNotBlank()) {
                    navController.navigate(GgriggriNavigationRouteUi.RequestDetail(requestId = request.requestId))
                }
            }
        )
    }
    composable<GgriggriNavigationRouteUi.MyPageTab> {
        MyPageScreen(
            onNavigateToGroupSetting = { navController.navigate(GgriggriNavigationRouteUi.SettingGroup) },
            onNavigateToLogin = { 
                navController.navigate(GgriggriNavigationRouteUi.Login) {
                    popUpTo(GgriggriNavigationRouteUi.Login) { inclusive = true }
                }
            }
        )
    }

    /********* archive ******************************************************/
    composable<GgriggriNavigationRouteUi.QuestionAnswer> {
        QuestionAnswerScreen()
    }
    composable<GgriggriNavigationRouteUi.QuestionList>{
        QuestionListScreen(
            onNavigateToQuestionAnswer = { questionId ->
                if (questionId.isNotBlank()) {
                    navController.navigate(GgriggriNavigationRouteUi.QuestionAnswer(questionDataId = questionId))
                }
            }
        )
    }
    composable<GgriggriNavigationRouteUi.RequestDetail> { backStackEntry ->
        val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
        RequestDetailScreen(
            requestId = requestId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
    composable<GgriggriNavigationRouteUi.RequestList>{
        RequestListScreen(
            onNavigateToRequestDetail = { requestId ->
                if(requestId.isNotBlank()){
                    navController.navigate(GgriggriNavigationRouteUi.RequestDetail(requestId = requestId))
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
            },
            onNavigationToDevLogin = {
                Log.d("AppNavigation", "onNavigationToDevLogin called")
                navController.navigate(GgriggriNavigationRouteUi.DevLogin)
            }
        )
    }

    composable<GgriggriNavigationRouteUi.DevLogin> {
        DevLoginScreen(
            onNavigationToGroup = {
                Log.d("AppNavigation", "DevLogin onNavigationToGroup called")
                navController.navigate(GgriggriNavigationRouteUi.Group)
            },
            onNavigationToHome = {
                Log.d("AppNavigation", "DevLogin onNavigationToHome called")
                navController.navigate(GgriggriNavigationRouteUi.HomeTab) {
                    popUpTo(GgriggriNavigationRouteUi.DevLogin) {inclusive = true}
                }
            }
        )
    }

    /********* group ********************************************************/
    composable<GgriggriNavigationRouteUi.Group> {
        GroupScreen(
            onNavigateToHome = {navController.navigate(GgriggriNavigationRouteUi.HomeTab)},
            onNavigateBack = { navController.popBackStack() },
        )
    }

    /********* main *********************************************************/
    composable <GgriggriNavigationRouteUi.Answer> {
        AnswerScreen(
            onNavigateBack = {navController.navigate(GgriggriNavigationRouteUi.HomeTab)}
        )
    }

    composable <GgriggriNavigationRouteUi.Request> {
        RequestScreen(
            onNavigateBack = {navController.navigate(GgriggriNavigationRouteUi.HomeTab)}
        )
    }

//    composable <GgriggriNavigationRouteUi.Response> {
//        ResponseScreen(
//            onNavigateBack = {navController.navigate(GgriggriNavigationRouteUi.HomeTab)}
//        )
//    }

    /********* setting ******************************************************/
    composable<GgriggriNavigationRouteUi.SettingGroup> {
        SettingGroupScreen(
            onNavigateToModifyGroupPw = {navController.navigate(GgriggriNavigationRouteUi.ModifyGroupPw)},
            onNavigateToModifyGroupName = {navController.navigate(GgriggriNavigationRouteUi.ModifyGroupName)},
            onNavigateToLeaveGroup = {
                navController.navigate(GgriggriNavigationRouteUi.Group) {
                    popUpTo(GgriggriNavigationRouteUi.Group) { inclusive = true }
                }
            }
        )
    }
    composable<GgriggriNavigationRouteUi.ModifyGroupPw> {
        ModifyGroupPwScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSettingGroup = { 
                navController.navigate(GgriggriNavigationRouteUi.SettingGroup) {
                    popUpTo(GgriggriNavigationRouteUi.SettingGroup) { inclusive = true }
                }
            }
        )
    }
    composable<GgriggriNavigationRouteUi.ModifyGroupName> {
        ModifyGroupNameScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSettingGroup = { 
                navController.navigate(GgriggriNavigationRouteUi.SettingGroup) {
                    popUpTo(GgriggriNavigationRouteUi.SettingGroup) { inclusive = true }
                }
            }
        )
    }

}