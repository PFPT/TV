package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.R
import com.pfpt.tv.playback.vod.session.VodUiState

@Composable
fun VodDetailHeader(
    state: VodUiState,
    onKeepClick: () -> Unit,
    onTitleClick: () -> Unit,
    onTitleLongClick: () -> Unit,
    onSiteClick: () -> Unit,
    onRemarkClick: () -> Unit,
    onDownloadClick: () -> Unit = {},
    onReverseClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onCopyContent: () -> Unit = {},
    showActions: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var directorLines by remember { mutableIntStateOf(1) }
    var actorLines by remember { mutableIntStateOf(1) }
    var contentLines by remember { mutableIntStateOf(3) }
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            text = state.title,
            color = Color.White,
            fontSize = 20.sp,
            maxLines = 3,
            modifier = Modifier.combinedClickable(onClick = onTitleClick, onLongClick = onTitleLongClick),
        )
        if (state.remark.isNotEmpty()) {
            Text(
                text = state.remark,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp).clickable(onClick = onRemarkClick),
            )
        }
        if (state.siteName.isNotEmpty()) {
            Text(
                text = state.siteName,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp).clickable(onClick = onSiteClick),
            )
        }
        if (state.year.isNotEmpty()) {
            Text(
                text = stringResource(R.string.detail_year, state.year),
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (state.area.isNotEmpty()) {
            Text(
                text = stringResource(R.string.detail_area, state.area),
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (state.type.isNotEmpty()) {
            Text(
                text = stringResource(R.string.detail_type, state.type),
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (state.director.isNotEmpty()) {
            Text(
                text = stringResource(R.string.detail_director, state.director),
                color = Color.White,
                fontSize = 14.sp,
                maxLines = directorLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { directorLines = if (directorLines == 1) Int.MAX_VALUE else 1 },
            )
        }
        if (state.actor.isNotEmpty()) {
            Text(
                text = stringResource(R.string.detail_actor, state.actor),
                color = Color.White,
                fontSize = 14.sp,
                maxLines = actorLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { actorLines = if (actorLines == 1) Int.MAX_VALUE else 1 },
            )
        }
        if (state.content.isNotEmpty()) {
            Text(
                text = state.content,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = contentLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .combinedClickable(
                        onClick = { contentLines = if (contentLines == 3) Int.MAX_VALUE else 3 },
                        onLongClick = onCopyContent,
                    ),
            )
        }
        TextButton(onClick = onKeepClick) {
            Text(
                text = stringResource(if (state.kept) R.string.keep_toggle_on else R.string.keep_toggle_off),
                color = Color.White,
            )
        }
        if (showActions) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onDownloadClick) {
                    Text(text = stringResource(R.string.detail_download), color = Color.White)
                }
                TextButton(onClick = onReverseClick) {
                    Text(text = stringResource(R.string.play_reverse), color = Color.White)
                }
                TextButton(onClick = onMoreClick) {
                    Text(text = stringResource(R.string.detail_episode), color = Color.White)
                }
            }
        }
    }
}
