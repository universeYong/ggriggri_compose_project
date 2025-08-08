package com.ahn.ggrigggri.common

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ahn.ggrigggri.navigation.bottom.BottomAppBarItem

@Composable
fun GgriggriBottomBar(
    items: List<BottomAppBarItem>,
    selectedTitle: String,
    onItemClick: (BottomAppBarItem) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.tabName == selectedTitle,
                onClick = { onItemClick(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.tabName
                    )
                },
                label = { Text(text = item.tabName)}
            )
        }
    }
}