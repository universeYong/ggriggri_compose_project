package com.ahn.ggrigggri

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.ahn.ggrigggri.common.GgriggriBottomBar
import com.ahn.ggrigggri.common.GgriggriTopBar
import com.ahn.ggrigggri.navigation.bottom.BottomAppBarItem
import com.ahn.ggrigggri.navigation.nav_graph.MainNavigationRoute
import com.ahn.ggrigggri.navigation.nav_graph.TopBarData
import com.ahn.ggrigggri.navigation.nav_graph.topBarAsRouteName
import com.ahn.ggriggri.screen.archive.memory.MemoryScreen
import com.ahn.ggriggri.screen.archive.questionanswer.QuestionAnswerScreen
import com.ahn.ggriggri.screen.auth.login.LoginScreen
import com.ahn.ggriggri.screen.auth.resetpw.ResetPwScreen
import com.ahn.ggriggri.screen.group.GroupScreen
import com.ahn.ggriggri.screen.main.home.HomeScreen
import com.ahn.ggriggri.screen.setting.modifygroupname.ModifyGroupNameScreen
import com.ahn.ggriggri.screen.setting.modifygrouppw.ModifyGroupPwScreen
import com.ahn.ggriggri.screen.setting.modifyuserpw.ModifyUserPwScreen
import com.ahn.ggriggri.screen.setting.mypage.MyPageScreen
import com.ahn.ggriggri.screen.setting.settinggroup.SettingGroupScreen
import com.ahn.ggriggri.screen.ui.auth.viewmodel.OAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryPointScreen() {

    val authViewModel: OAuthViewModel = viewModel()

    val navController = rememberNavController()
    val context = LocalContext.current

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

    val topBarTitle = if (topBarData.title != 0) stringResource(topBarData.title) else ""

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            GgriggriTopBar(
                topBarData = topBarData,
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
            startDestination = MainNavigationRoute.Login,
            modifier = Modifier.padding(paddingValues = paddingValues)
        ) {

            composable<MainNavigationRoute.MemoryTab> {
                MemoryScreen()
            }
            composable<MainNavigationRoute.HomeTab> {
                HomeScreen()
            }
            composable<MainNavigationRoute.MyPageTab> {
                MyPageScreen()
            }

            /********* archive ******************************************************/
            composable<MainNavigationRoute.QuestionAnswer> {
                QuestionAnswerScreen()
            }

            /********* auth *********************************************************/
            composable<MainNavigationRoute.PasswordReset> {
                ResetPwScreen()
            }

            composable<MainNavigationRoute.Login> {
                LoginScreen(
                    authViewModel = authViewModel,
                   onNavigationToGroup = {
                       navController.navigate(MainNavigationRoute.Group)
                   },
                    onNavigationToHome = {
                        navController.navigate(MainNavigationRoute.HomeTab) {
                            popUpTo(MainNavigationRoute.Login) {inclusive = true}
                        }
                    }
                )
            }

            /********* group ********************************************************/
            composable<MainNavigationRoute.Group> {
                GroupScreen(authViewModel = authViewModel)
            }

            /********* main *********************************************************/


            /********* setting ******************************************************/
            composable<MainNavigationRoute.SettingGroup> {
                SettingGroupScreen()
            }
            composable<MainNavigationRoute.ModifyUserPw> {
                ModifyUserPwScreen()
            }
            composable<MainNavigationRoute.ModifyGroupPw> {
                ModifyGroupPwScreen()
            }
            composable<MainNavigationRoute.ModifyGroupName> {
                ModifyGroupNameScreen()
            }

        }
    }
}
