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
import com.ahn.ggriggri.screen.navigation.GgriggriNavigationGraph
import com.ahn.ggriggri.screen.navigation.GgriggriNavigationRouteUi
import com.ahn.ggriggri.screen.navigation.MainFactories
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

    val factories = MainFactories(
        home = appContainer.provideHomeViewModelFactory(),
        answer = appContainer.provideAnswerViewModelFactory(),
        archive = appContainer.provideArchiveViewModelFactory(),
        questionAnswer = appContainer.provideQuestionAnswerViewModelFactory(),
        myPage = appContainer.provideMyPageViewModelFactory(),
        auth = appContainer.provideOAuthViewModelFactory()
    )

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
        ){
            GgriggriNavigationGraph(navController, factories)
        }
    }
}
