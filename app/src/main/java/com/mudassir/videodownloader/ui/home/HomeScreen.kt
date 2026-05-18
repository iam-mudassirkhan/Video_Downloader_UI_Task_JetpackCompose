package com.mudassir.videodownloader.ui.home


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable

fun HomeScreen(onNavigateToTrimmer: () -> Unit = {}) {

    var selectedTab by remember { mutableIntStateOf(0) }


    var showBanner by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {

            BottomSection(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            item { Header() }

            item { Spacer(Modifier.height(4.dp)) }

            item { SearchSection() }

            item { Spacer(Modifier.height(8.dp)) }

            item { SocialRow() }

            item {
                AnimatedVisibility(
                    visible = showBanner,
                    exit = shrinkVertically() + fadeOut()
                ) {
                    DownloadCard(onDismiss = { showBanner = false })
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            item { TrendingSection(onNavigateToTrimmer) }

            item { SocialTools() }
        }
    }
}

