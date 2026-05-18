package com.mudassir.videodownloader.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudassir.videodownloader.R
import com.mudassir.videodownloader.ui.home.HomeColors.BioBg
import com.mudassir.videodownloader.ui.home.HomeColors.BioTint
import com.mudassir.videodownloader.ui.home.HomeColors.CaptionBg
import com.mudassir.videodownloader.ui.home.HomeColors.CaptionTint
import com.mudassir.videodownloader.ui.home.HomeColors.DotActive
import com.mudassir.videodownloader.ui.home.HomeColors.DotInactive
import com.mudassir.videodownloader.ui.home.HomeColors.HashtagBg
import com.mudassir.videodownloader.ui.home.HomeColors.HashtagTint
import com.mudassir.videodownloader.ui.home.HomeColors.LightGreyForBG
import com.mudassir.videodownloader.ui.home.HomeColors.NavGray
import com.mudassir.videodownloader.ui.home.HomeColors.NavOrange
import com.mudassir.videodownloader.ui.home.HomeColors.Orange


@Composable
fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Video Downloader",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter= painterResource(
                    R.drawable.ic_pro_icon
                ),
                contentDescription = "Premium",
                tint = Color(0xFFFFCC00),
                modifier = Modifier.size(26.dp)
            )
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun SearchSection() {

    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Row(
            modifier = Modifier
                .weight(1f)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(20.dp)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(LightGreyForBG)
                .border(
                    1.dp,
                    Color(0xFFEEEEEE),
                    RoundedCornerShape(20.dp)
                )
                .padding(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),

            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Image(
                    painter = painterResource(R.drawable.google),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )

                Text(
                    text = "▾",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }


            Box(
                modifier = Modifier.weight(1f)
            ) {

                if (text.isEmpty()) {

                    Text(
                        text = "Search Or Paste URL",
                        fontSize = 13.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }

                BasicTextField(
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    singleLine = true,

                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        color = Color.Black
                    ),

                    cursorBrush = SolidColor(Orange)
                )
            }


            Image(
                painter = painterResource(
                    R.drawable.ic_qrcodescanner
                ),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }


        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(
                    RoundedCornerShape(8.dp)
                )
                .border(
                    2.dp,
                    Orange,
                    RoundedCornerShape(8.dp)
                ),

            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "2",
                color = Orange,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SocialRow() {

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                1.dp,
                Color(0xFFE9E9E9),
                RoundedCornerShape(18.dp)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 14.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceAround,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            socialApps.forEach {

                SocialCard(it)

            }
        }

        Spacer(Modifier.height(12.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.Center
        ) {

            repeat(3) { index ->

                val active = index==0

                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(
                            width =
                                if(active)16.dp
                                else 7.dp,

                            height = 5.dp
                        )
                        .clip(
                            RoundedCornerShape(5.dp)
                        )
                        .background(
                            if(active)
                                DotActive
                            else
                                DotInactive
                        )
                )
            }
        }
    }
}

@Composable
fun SocialCard(item: SocialItem) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.spacedBy(5.dp),

            modifier = Modifier
                .width(58.dp)
                .clickable { }
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {

                Image(
                    painter =
                        painterResource(item.icon),

                    contentDescription = item.title,

                    modifier =
                        Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text=item.title,

                fontSize=10.sp,

                color= Color(0xFF555555),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign= TextAlign.Center
            )
        }


    }
}


@Composable
fun DownloadCard(
    onDismiss: () -> Unit
) {

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(LightGreyForBG)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {

        Row(
            modifier = Modifier.align(Alignment.Center),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {

            Image(
                painter = painterResource(
                    R.drawable.ic_download_help
                ),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )

            Text(
                text = "How to Download?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Orange
            )
        }


        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = Color(0xFF888888),

            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(18.dp)
                .clickable(
                    interactionSource = remember {
                        MutableInteractionSource()
                    },
                    indication = null
                ) {
                    onDismiss()
                }
        )
    }
}

@Composable
fun TrendingSection(
    onNavigateToTrimmer: () -> Unit
)  {
    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "🔥", fontSize = 18.sp)
                Text(
                    text = "Trending",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Text(
                text = "See All",
                fontSize = 13.sp,
                color = Orange,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(trendingImages) { imageRes ->
                TrendingCard(imageRes = imageRes, onClick = onNavigateToTrimmer)

            }
        }

        Spacer(Modifier.height(16.dp))
    }
}


@Composable
fun TrendingCard(imageRes: Int, onClick: () -> Unit = {}) {
    Box(
        modifier=
            Modifier
                .size(
                    width = 110.dp,
                    height = 160.dp
                )

                .clip(RoundedCornerShape(12.dp))
                .clickable {onClick() },

        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = "Trending video",
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = Color.Black,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}


@Composable
fun SocialTools() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Social Tools",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            ToolItem(
                label = "Hashtag",
                iconRes = R.drawable.ic_hashtag,
                iconTint = HashtagTint,
                bgColor = HashtagBg,
            )

            ToolItem(
                label = "Caption",
                iconRes = R.drawable.ic_caption,
                iconTint = CaptionTint,
                bgColor = CaptionBg
            )
            ToolItem(
                label = "Bio",
                iconRes = R.drawable.ic_bio,
                iconTint = BioTint,
                bgColor = BioBg
            )
        }
    }
}

@Composable
fun ToolItem(
    label: String,
    iconRes: Int,
    iconTint: Color,
    bgColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .width(80.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(30.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(iconTint)
            )
        }

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun BottomSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {



    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(
            elevation = 8.dp,
            spotColor = Color.Black.copy(alpha = 0.1f)
        )
    ) {

        items.forEachIndexed { index, item ->

            val isSelected = selectedTab == index

            NavigationBarItem(

                selected = isSelected,

                onClick = {
                    onTabSelected(index)
                },

                icon = {

                    Icon(
                        painter = painterResource(
                            id = if (isSelected)
                                item.selectedIcon
                            else
                                item.unselectedIcon
                        ),

                        contentDescription = item.label,

                        modifier = Modifier.size(24.dp),

                        tint =
                            if (isSelected)
                                NavOrange
                            else
                                NavGray
                    )
                },

                label = {

                    Text(
                        text = item.label,

                        fontSize = 11.sp,

                        fontWeight =
                            if (isSelected)
                                FontWeight.SemiBold
                            else
                                FontWeight.Normal
                    )
                },

                colors = NavigationBarItemDefaults.colors(

                    selectedIconColor = NavOrange,

                    selectedTextColor = NavOrange,

                    unselectedIconColor = NavGray,

                    unselectedTextColor = NavGray,

                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
