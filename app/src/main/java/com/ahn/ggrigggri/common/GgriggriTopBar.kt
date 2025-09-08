package com.ahn.ggrigggri.common

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ahn.ggrigggri.navigation.nav_graph.TopBarData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GgriggriTopBar(
    topBarData: TopBarData,
) {
    CenterAlignedTopAppBar(
        title = {
            if (topBarData.title == 0) Text(" ")
            else Text(stringResource(id = topBarData.title))
                },
        navigationIcon = {
            topBarData.titleLeftIcon?.let { icon ->
                IconButton(onClick = topBarData.IconOnClick ) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            }
        },
        actions = {
            topBarData.titleRightIcon?.let  { icon ->
                IconButton(onClick = topBarData.rightIconOnClick) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            }
        }
    )
}