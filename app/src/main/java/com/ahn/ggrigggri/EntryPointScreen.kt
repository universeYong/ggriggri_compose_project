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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.ahn.ggriggri.screen.navigation.GgriggriBottomBar
import com.ahn.ggriggri.screen.navigation.GgriggriTopBar
import com.ahn.ggriggri.screen.navigation.BottomAppBarItem
import com.ahn.ggriggri.screen.navigation.GgriggriNavigationGraph
import com.ahn.ggriggri.screen.navigation.GgriggriNavigationRouteUi
import com.ahn.ggriggri.screen.navigation.TopBarData
import com.ahn.ggriggri.screen.navigation.topBarAsRouteName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryPointScreen() {

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

    val finalTopBarData = remember(topBarData, navBackStackEntry?.destination?.route) {

        if (topBarData.titleLeftIcon != null) {
            topBarData.copy(IconOnClick = {
                Log.d("Navigation", "TopBar back button clicked")
                Log.d("Navigation", "Current route: $currentRoute")
                Log.d("Navigation", "Route contains Group: ${currentRoute?.contains("Group")}")
                
                // Group 화면인지 확인 (더 정확한 체크)
                val isGroupScreen = currentRoute?.contains("Group") == true || 
                                  currentRoute?.contains("ggriggri_compose.Group") == true
                
                if (isGroupScreen) {
                    Log.d("Navigation", "Navigating to Login from Group")
                    navController.navigate(GgriggriNavigationRouteUi.Login) {
                        popUpTo(GgriggriNavigationRouteUi.Login) { inclusive = true }
                    }
                } else {
                    Log.d("Navigation", "Using popBackStack")
                    navController.popBackStack()
                }
            })
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
            GgriggriNavigationGraph(navController)
        }
    }
}
