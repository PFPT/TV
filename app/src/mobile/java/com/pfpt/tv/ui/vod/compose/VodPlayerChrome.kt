package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object VodPlayerChrome {
    val CapsuleBg = Color(0x99000000)
    val CapsuleShape = RoundedCornerShape(20.dp)
    val TrackActive = Color.White
    val TrackInactive = Color.White.copy(alpha = 0.35f)
    val TrackHeight = 3.dp
    val IconSize = 40.dp
    val PlayIconSize = 52.dp
    val TitleSize = 15.sp
    val MetaSize = 12.sp
    val HudLabelSize = 14.sp
    val BarHorizontal = 12.dp
    val BarVertical = 10.dp

    val TopScrim = Brush.verticalGradient(
        colors = listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent),
    )
    val BottomScrim = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
    )
}

@Composable
internal fun PlayerHudCapsule(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .clip(VodPlayerChrome.CapsuleShape)
            .background(VodPlayerChrome.CapsuleBg)
            .padding(start = 12.dp, end = 14.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}
