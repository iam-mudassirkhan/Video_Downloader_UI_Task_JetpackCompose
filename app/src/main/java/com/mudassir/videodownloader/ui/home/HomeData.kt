package com.mudassir.videodownloader.ui.home


import com.mudassir.videodownloader.R

// Social apps shown on home screen
val socialApps = listOf(

    SocialItem(
        "Facebook",
        R.drawable.ic_facebook
    ),

    SocialItem(
        "Instagram",
        R.drawable.ic_instagram
    ),

    SocialItem(
        "TikTok",
        R.drawable.ic_tiktok
    ),

    SocialItem(
        "Daily Motion",
        R.drawable.ic_daily_motion
    ),

    SocialItem(
        "Twitter",
        R.drawable.ic_x
    )
)

val items = listOf(

    NavItem(
        label = "Home",
        selectedIcon = R.drawable.ic_home,
        unselectedIcon = R.drawable.ic_home
    ),

    NavItem(
        label = "Download",
        selectedIcon = R.drawable.ic_download,
        unselectedIcon = R.drawable.ic_download
    ),

    NavItem(
        label = "Watch",
        selectedIcon = R.drawable.ic_watch,
        unselectedIcon = R.drawable.ic_watch
    ),

    NavItem(
        label = "Settings",
        selectedIcon = R.drawable.ic_setting,
        unselectedIcon = R.drawable.ic_setting
    )
)

// Trending cards
// Later this can come from API response
val trendingImages = listOf(

    R.drawable.trending1,
    R.drawable.trend2,
    R.drawable.trend3

)