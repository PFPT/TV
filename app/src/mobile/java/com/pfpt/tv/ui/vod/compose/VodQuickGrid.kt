package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.playback.vod.session.VodQuickItem

@Composable
fun VodQuickGrid(
    items: List<VodQuickItem>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val columns = if (LocalConfiguration.current.smallestScreenWidthDp >= 600) 4 else 2
    Column(modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
        items.chunked(columns).forEachIndexed { rowIndex, row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEachIndexed { column, item ->
                    val index = rowIndex * columns + column
                    VodQuickCard(
                        item = item,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onClick(index) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                repeat(columns - row.size) {
                    Spacer(Modifier.weight(1f).padding(8.dp))
                }
            }
        }
    }
}

@Composable
private fun VodQuickCard(
    item: VodQuickItem,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = item.name,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (item.site.isNotEmpty()) {
            Text(
                text = item.site,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (item.remark.isNotEmpty()) {
            Text(
                text = item.remark,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
