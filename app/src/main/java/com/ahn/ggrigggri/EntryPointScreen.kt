package com.ahn.ggrigggri

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ahn.ggriggri.screen.navigation.GgriggriBottomBar
import com.ahn.ggriggri.screen.navigation.GgriggriTopBar
import com.ahn.ggriggri.screen.navigation.BottomAppBarItem
import com.ahn.ggriggri.screen.archive.memory.MemoryScreen
import com.ahn.ggriggri.screen.archive.questionanswer.QuestionAnswerScreen
import com.ahn.ggriggri.screen.archive.questionlist.QuestionListScreen
import com.ahn.ggriggri.screen.auth.login.LoginScreen
import com.ahn.ggriggri.screen.auth.resetpw.ResetPwScreen
import com.ahn.ggriggri.screen.group.GroupScreen
import com.ahn.ggriggri.screen.main.answer.AnswerScreen
import com.ahn.ggriggri.screen.main.home.HomeScreen
import com.ahn.ggriggri.screen.navigation.GgriggriNavigationRouteUi
import com.ahn.ggriggri.screen.navigation.TopBarData
import com.ahn.ggriggri.screen.navigation.topBarAsRouteName
import com.ahn.ggriggri.screen.setting.modifygroupname.ModifyGroupNameScreen
import com.ahn.ggriggri.screen.setting.modifygrouppw.ModifyGroupPwScreen
import com.ahn.ggriggri.screen.setting.mypage.MyPageScreen
import com.ahn.ggriggri.screen.setting.settinggroup.SettingGroupScreen
import com.ahn.ggriggri.screen.ui.auth.viewmodel.OAuthViewModel
import com.ahn.ggriggri.screen.ui.auth.viewmodel.OAuthViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryPointScreen() {

    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as ggriggriAplication
    val appContainer = application.appContainer // AppContainer 가져오기

    // Factory 생성
    val oauthViewModelFactory = OAuthViewModelFactory(
        application,
        application.sessionManager,
        application.userRepository,
    )
    // ViewModel 생성
    val authViewModel: OAuthViewModel = viewModel(factory = oauthViewModelFactory)

    //Bottom App Bar items
    val bottomAppBarItems = remember {
        BottomAppBarItem.fetchBottomAppBarItems(context)
    }

    /**
     * currentBackStackEntryAsState()은
     * 현재 Navigation Stack Entries 를 관찰하며 갱신시 재구성이 일어남
     */
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topBarData = navBackStackEntry?.topBarAsRouteName ?: TopBarData()

    val finalTopBarData = remember(topBarData, navBackStackEntry?.destination?.route) {
        val currentRouteName = navBackStackEntry?.destination?.route
        // 왼쪽 아이콘이 있을때 뒤로가기 액션
        if (topBarData.titleLeftIcon != null) {
            topBarData.copy(IconOnClick = { navController.popBackStack() })
        } else {
            topBarData
        }
    }


    val topBarTitle = if (topBarData.title != 0) stringResource(topBarData.title) else ""

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            GgriggriTopBar(
                topBarData = finalTopBarData,
            )
        },
        bottomBar = {
            if (currentRoute != null &&
                (currentRoute.contains("HomeTab")
                || currentRoute.contains("MemoryTab")
                || currentRoute.contains("MyPageTab"))) {
                GgriggriBottomBar(
                    items = bottomAppBarItems,
                    selectedTitle = topBarTitle,
                    onItemClick = { bottomItem ->
                        navController.navigate(route = bottomItem.destination) {
                            popUpTo(route = bottomItem.destination) {
                                saveState = true
                            }
                            /**
                             * 동일한 목적지가 스택 최상단에 있으면 새 인스턴스를 생성 X
                             */
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = GgriggriNavigationRouteUi.Login,
            modifier = Modifier.padding(paddingValues = paddingValues)
        ) {

            composable<GgriggriNavigationRouteUi.MemoryTab> {
                MemoryScreen(
                    archiveViewModel = viewModel(factory = appContainer.provideArchiveViewModelFactory()),
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
                HomeScreen(homeViewmodel = viewModel(factory = appContainer.provideHomeViewModelFactory()),
                    onNavigationToAnswer = {navController.navigate(GgriggriNavigationRouteUi.Answer)})
            }
            composable<GgriggriNavigationRouteUi.MyPageTab> {
                MyPageScreen(
                    myPageviewModel = viewModel(factory = appContainer.provideMyPageViewModelFactory())
                )
            }

            /********* archive ******************************************************/
            composable<GgriggriNavigationRouteUi.QuestionAnswer> {
                QuestionAnswerScreen(
                    questionAnswerViewModel = viewModel(factory = appContainer.provideQuestionAnswerViewModelFactory()),
                )
            }
            composable<GgriggriNavigationRouteUi.QuestionList>{
                QuestionListScreen(
                    archiveViewModel = viewModel(factory = appContainer.provideArchiveViewModelFactory()),
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
                    authViewModel = authViewModel,
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
                GroupScreen(authViewModel = authViewModel)
            }

            /********* main *********************************************************/
            composable <GgriggriNavigationRouteUi.Answer> {
                AnswerScreen(
                    answerViewModel = viewModel(factory = appContainer.provideAnswerViewModelFactory()),
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
    }
}
