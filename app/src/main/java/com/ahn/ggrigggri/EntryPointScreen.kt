package com.ahn.ggrigggri

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ahn.ggriggri.screen.navigation.BottomAppBarItem
import com.ahn.ggriggri.screen.navigation.GgriggriBottomBar
import com.ahn.ggriggri.screen.navigation.GgriggriNavigationGraph
import com.ahn.ggriggri.screen.navigation.GgriggriNavigationRouteUi
import com.ahn.ggriggri.screen.navigation.GgriggriTopBar
import com.ahn.ggriggri.screen.navigation.TopBarData
import com.ahn.ggriggri.screen.navigation.topBarAsRouteName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryPointScreen(
    isLoggedIn: Boolean? = null,
    notificationRoute: NotificationRoute? = null,
    onNotificationRouteConsumed: () -> Unit = {},
) {
    if (isLoggedIn == null) {
        SplashScreen()
        return
    }

    val navController = rememberNavController()
    val context = LocalContext.current

    val bottomAppBarItems = remember {
        BottomAppBarItem.fetchBottomAppBarItems(context)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val topBarData = navBackStackEntry?.topBarAsRouteName ?: TopBarData()

    val isBottomTabDestination = navBackStackEntry?.destination?.hierarchy?.any { destination ->
        val route = destination.route.orEmpty()
        route.contains("HomeTab") || route.contains("MemoryTab") || route.contains("MyPageTab")
    } == true

    BackHandler(enabled = isBottomTabDestination) {
        (context as? Activity)?.finish()
    }

    LaunchedEffect(notificationRoute, isLoggedIn) {
        if (!isLoggedIn) {
            onNotificationRouteConsumed()
            return@LaunchedEffect
        }

        val route = notificationRoute ?: return@LaunchedEffect

        navController.navigate(GgriggriNavigationRouteUi.HomeTab) {
            popUpTo(GgriggriNavigationRouteUi.HomeTab) { inclusive = false }
            launchSingleTop = true
        }

        when (route.type) {
            "daily_question" -> {
                navController.navigate(GgriggriNavigationRouteUi.Answer) {
                    launchSingleTop = true
                }
            }

            "request" -> {
                val requestId = route.requestId.orEmpty()
                if (requestId.isNotBlank()) {
                    navController.navigate(
                        GgriggriNavigationRouteUi.Response(requestDocumentId = requestId)
                    ) {
                        launchSingleTop = true
                    }
                }
            }

            "response" -> {
                val requestId = route.requestId.orEmpty()
                if (requestId.isNotBlank()) {
                    navController.navigate(
                        GgriggriNavigationRouteUi.RequestDetail(requestId = requestId)
                    ) {
                        launchSingleTop = true
                    }
                }
            }
        }

        onNotificationRouteConsumed()
    }

    val finalTopBarData = remember(topBarData, navBackStackEntry?.destination?.route) {
        if (topBarData.titleLeftIcon != null) {
            topBarData.copy(IconOnClick = {
                Log.d("Navigation", "TopBar back button clicked")
                Log.d("Navigation", "Current route: $currentRoute")
                val isOnboardingGroupRoute = currentRoute == "Group" ||
                    currentRoute?.endsWith(".Group") == true

                if (isOnboardingGroupRoute) {
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
            GgriggriTopBar(topBarData = finalTopBarData)
        },
        bottomBar = {
            if (currentRoute != null &&
                (currentRoute.contains("HomeTab") ||
                    currentRoute.contains("MemoryTab") ||
                    currentRoute.contains("MyPageTab"))
            ) {
                GgriggriBottomBar(
                    items = bottomAppBarItems,
                    selectedTitle = topBarTitle,
                    onItemClick = { bottomItem ->
                        navController.navigate(route = bottomItem.destination) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) {
                GgriggriNavigationRouteUi.HomeTab
            } else {
                GgriggriNavigationRouteUi.Login
            },
            modifier = Modifier.padding(paddingValues = paddingValues),
        ) {
            GgriggriNavigationGraph(navController)
        }
    }
}
