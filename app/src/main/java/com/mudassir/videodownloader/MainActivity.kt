package com.mudassir.videodownloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mudassir.videodownloader.ui.home.HomeScreen
import com.mudassir.videodownloader.ui.theme.VideoDownloaderTheme
import com.mudassir.videodownloader.ui.trimmer.TrimmerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            VideoDownloaderTheme {

                val navController = rememberNavController()

                NavHost(
                    navController    = navController,
                    startDestination = "home"
                ) {

                    composable("home") {
                        HomeScreen(
                            // When a trending card is tapped, go to trimmer Screen
                            onNavigateToTrimmer = {
                                navController.navigate("trimmer")
                            }
                        )
                    }

                    composable("trimmer") {
                        TrimmerScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

