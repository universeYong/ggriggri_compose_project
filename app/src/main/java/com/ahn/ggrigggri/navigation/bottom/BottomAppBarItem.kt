package com.ahn.ggrigggri.navigation.bottom

import android.content.Context
import android.widget.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.ui.graphics.vector.ImageVector
import com.ahn.common_ui.R
import com.ahn.ggrigggri.navigation.nav_graph.MainNavigationRoute

data class BottomAppBarItem(
    val tabName: String = "",
    val icon: ImageVector = Icons.Outlined.Home,
    val destination: MainNavigationRoute = MainNavigationRoute.HomeTab,
) {
    companion object{
        fun fetchBottomAppBarItems(context: Context) = listOf(
            BottomAppBarItem(
                tabName = context.getString(R.string.memory_tab),
                icon = Icons.Outlined.PictureAsPdf,
                destination = MainNavigationRoute.MemoryTab // 인자가있는 data class라면 () 붙이기
            ),
            BottomAppBarItem(
                tabName = context.getString(R.string.home_tab),
            ),
            BottomAppBarItem(
                tabName = context.getString(R.string.myPage_tab),
                icon = Icons.Outlined.Person,
                destination = MainNavigationRoute.MyPageTab
            )
        )

    }
}
