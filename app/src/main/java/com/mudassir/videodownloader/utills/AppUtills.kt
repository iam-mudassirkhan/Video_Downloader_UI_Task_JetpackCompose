package com.mudassir.videodownloader.utills

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

object AppUtills {

    @Composable
    fun WindowInsetsController(){
        val view = LocalView.current

        SideEffect {
            val window = (view.context as Activity).window

            WindowCompat.setDecorFitsSystemWindows(window, false)

            val controller = WindowInsetsControllerCompat(window, window.decorView)

            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }

    }
}