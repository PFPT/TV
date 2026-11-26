package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.R
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

data class DanmakuStyleRows(
    val shadow: Boolean,
    val stroke: Boolean,
    val projection: Boolean,
)

fun danmakuStyleRows(mode: Int): DanmakuStyleRows {
    return DanmakuStyleRows(shadow = mode == 1, stroke = mode == 2, projection = mode == 3)
}

fun danmakuColorHintVisible(mode: Int): Boolean {
    return mode != 1
}

fun danmakuStyleIndex(mode: Int): Int {
    return if (mode in 0..3) mode else 2
}

fun danmakuColorIndex(mode: Int): Int {
    return when (mode) {
        2 -> 1
        3 -> 2
        else -> 0
    }
}

fun danmakuStyleMode(index: Int): Int {
    return if (index in 0..3) index else 2
}

fun danmakuColorMode(index: Int): Int {
    return when (index) {
        1 -> 2
        2 -> 3
        else -> 1
    }
}

fun danmakuLinesLabel(value: Int, auto: String): String {
    return if (value == 0) auto else value.toString()
}

fun sliderSteps(from: Float, to: Float, step: Float): Int {
    if (step <= 0f) return 0
    return (((to - from) / step).roundToInt() - 1).coerceAtLeast(0)
}

fun danmakuDecimalLabel(value: Float, pattern: String): String {
    return String.format(Locale.getDefault(), pattern, value)
}

fun danmakuMsLabel(valueMs: Float): String {
    return String.format(Locale.getDefault(), "%.1fs", valueMs / 1000f)
}

fun blankDanmakuSetting(): VodDanmakuSettingState {
    return VodDanmakuSettingState(
        tab = 0,
        textBold = false,
        textScale = 1f,
        transparency = 0f,
        shadowTransparency = 0f,
        strokeWidth = 0.05f,
        projectionOffsetX = 0.02f,
        projectionOffsetY = 0.02f,
        projectionTransparency = 0f,
        styleMode = 2,
        colorMode = 1,
        timeOffsetMs = 0f,
        durationMs = 3000f,
        fixedDurationMs = 2000f,
        maxOnScreen = 10f,
        scrollAreaRatio = 0.1f,
        scrollGapRatio = 0f,
        lineSpacing = 1f,
        maxScrollLines = 0f,
        maxTopLines = 0f,
        maxBottomLines = 0f,
        showScroll = true,
        showTop = true,
        showBottom = true,
        showReverse = true,
        showPositioned = true,
        showSubtitle = true,
        showSpecial = true,
    )
}

data class VodDanmakuSettingState(
    val tab: Int,
    val textBold: Boolean,
    val textScale: Float,
    val transparency: Float,
    val shadowTransparency: Float,
    val strokeWidth: Float,
    val projectionOffsetX: Float,
    val projectionOffsetY: Float,
    val projectionTransparency: Float,
    val styleMode: Int,
    val colorMode: Int,
    val timeOffsetMs: Float,
    val durationMs: Float,
    val fixedDurationMs: Float,
    val maxOnScreen: Float,
    val scrollAreaRatio: Float,
    val scrollGapRatio: Float,
    val lineSpacing: Float,
    val maxScrollLines: Float,
    val maxTopLines: Float,
    val maxBottomLines: Float,
    val showScroll: Boolean,
    val showTop: Boolean,
    val showBottom: Boolean,
    val showReverse: Boolean,
    val showPositioned: Boolean,
    val showSubtitle: Boolean,
    val showSpecial: Boolean,
)

object VodDanmakuSettingIds {
    const val TAB = 1
    const val RESET = 2
    const val BOOL = 3
    const val FLOAT = 4
    const val MODE = 5
}

object VodDanmakuSettingKeys {
    const val TEXT_BOLD = 1
    const val SHOW_SCROLL = 2
    const val SHOW_TOP = 3
    const val SHOW_BOTTOM = 4
    const val SHOW_REVERSE = 5
    const val SHOW_POSITIONED = 6
    const val SHOW_SUBTITLE = 7
    const val SHOW_SPECIAL = 8
    const val TEXT_SCALE = 9
    const val TRANSPARENCY = 10
    const val SHADOW = 11
    const val STROKE = 12
    const val PROJECTION_X = 13
    const val PROJECTION_Y = 14
    const val PROJECTION_ALPHA = 15
    const val TIME_OFFSET = 16
    const val DURATION = 17
    const val FIXED_DURATION = 18
    const val MAX_ON_SCREEN = 19
    const val SCROLL_AREA = 20
    const val SCROLL_GAP = 21
    const val LINE_SPACING = 22
    const val MAX_SCROLL = 23
    const val MAX_TOP = 24
    const val MAX_BOTTOM = 25
    const val STYLE = 26
    const val COLOR = 27
}

data class VodDanmakuSettingEvent(
    val id: Int,
    val key: Int = 0,
    val tab: Int = 0,
    val floatValue: Float = 0f,
    val boolValue: Boolean = false,
    val intValue: Int = 0,
)

fun interface VodDanmakuSettingAction {
    fun onEvent(event: VodDanmakuSettingEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanmakuSettingSheet(
    state: VodDanmakuSettingState,
    onEvent: (VodDanmakuSettingEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = VodPickerSheetContainer,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.danmaku_setting),
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onEvent(VodDanmakuSettingEvent(VodDanmakuSettingIds.RESET, tab = state.tab)) }) {
                    Text(stringResource(R.string.danmaku_reset), color = Color.White)
                }
            }
            SettingTabs(state.tab) { index ->
                onEvent(VodDanmakuSettingEvent(VodDanmakuSettingIds.TAB, tab = index))
            }
            when (state.tab) {
                0 -> AppearanceSettings(state, onEvent)
                1 -> TimingSettings(state, onEvent)
                2 -> DensitySettings(state, onEvent)
                else -> DisplaySettings(state, onEvent)
            }
        }
    }
}

@Composable
private fun SettingTabs(tab: Int, onTab: (Int) -> Unit) {
    val labels = listOf(
        stringResource(R.string.danmaku_section_appearance),
        stringResource(R.string.danmaku_section_timing),
        stringResource(R.string.danmaku_section_density),
        stringResource(R.string.danmaku_section_display),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val on = index == tab
            Text(
                text = label,
                color = if (on) Color.Black else Color.White,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (on) Color.White else Color.White.copy(alpha = 0.12f))
                    .clickable { onTab(index) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun AppearanceSettings(state: VodDanmakuSettingState, onEvent: (VodDanmakuSettingEvent) -> Unit) {
    val rows = danmakuStyleRows(state.styleMode)
    SettingSwitch(stringResource(R.string.danmaku_text_bold), state.textBold) {
        onEvent(boolEvent(VodDanmakuSettingKeys.TEXT_BOLD, state.tab, it))
    }
    SettingSlider(stringResource(R.string.danmaku_text_size), state.textScale, 0.5f, 3f, 0.1f, danmakuDecimalLabel(snapOffset(state.textScale, 0.5f, 3f, 0.1f), "%.1f")) {
        onEvent(floatEvent(VodDanmakuSettingKeys.TEXT_SCALE, state.tab, it))
    }
    SettingSlider(stringResource(R.string.danmaku_transparency), state.transparency, 0f, 0.9f, 0.05f, danmakuDecimalLabel(snapOffset(state.transparency, 0f, 0.9f, 0.05f), "%.2f")) {
        onEvent(floatEvent(VodDanmakuSettingKeys.TRANSPARENCY, state.tab, it))
    }
    Text(stringResource(R.string.danmaku_style_mode), color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(start = 24.dp, top = 8.dp))
    VodChipRow(
        labels = listOf(
            stringResource(R.string.danmaku_style_none),
            stringResource(R.string.danmaku_style_shadow),
            stringResource(R.string.danmaku_style_stroke),
            stringResource(R.string.danmaku_style_projection),
        ),
        selectedIndex = danmakuStyleIndex(state.styleMode),
        onClick = { index ->
            onEvent(modeEvent(VodDanmakuSettingKeys.STYLE, state.tab, danmakuStyleMode(index)))
        },
    )
    if (rows.shadow) {
        SettingSlider(stringResource(R.string.danmaku_shadow_transparency), state.shadowTransparency, 0f, 0.9f, 0.05f, danmakuDecimalLabel(snapOffset(state.shadowTransparency, 0f, 0.9f, 0.05f), "%.2f")) {
            onEvent(floatEvent(VodDanmakuSettingKeys.SHADOW, state.tab, it))
        }
    }
    if (rows.stroke) {
        SettingSlider(stringResource(R.string.danmaku_stroke_width), state.strokeWidth, 0.05f, 0.3f, 0.01f, danmakuDecimalLabel(snapOffset(state.strokeWidth, 0.05f, 0.3f, 0.01f), "%.2f")) {
            onEvent(floatEvent(VodDanmakuSettingKeys.STROKE, state.tab, it))
        }
    }
    if (rows.projection) {
        SettingSlider(stringResource(R.string.danmaku_projection_offset_x), state.projectionOffsetX, 0.02f, 0.15f, 0.01f, danmakuDecimalLabel(snapOffset(state.projectionOffsetX, 0.02f, 0.15f, 0.01f), "%.2f")) {
            onEvent(floatEvent(VodDanmakuSettingKeys.PROJECTION_X, state.tab, it))
        }
        SettingSlider(stringResource(R.string.danmaku_projection_offset_y), state.projectionOffsetY, 0.02f, 0.15f, 0.01f, danmakuDecimalLabel(snapOffset(state.projectionOffsetY, 0.02f, 0.15f, 0.01f), "%.2f")) {
            onEvent(floatEvent(VodDanmakuSettingKeys.PROJECTION_Y, state.tab, it))
        }
        SettingSlider(stringResource(R.string.danmaku_projection_transparency), state.projectionTransparency, 0f, 0.9f, 0.05f, danmakuDecimalLabel(snapOffset(state.projectionTransparency, 0f, 0.9f, 0.05f), "%.2f")) {
            onEvent(floatEvent(VodDanmakuSettingKeys.PROJECTION_ALPHA, state.tab, it))
        }
    }
    Text(stringResource(R.string.danmaku_color_mode), color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(start = 24.dp, top = 8.dp))
    VodChipRow(
        labels = listOf(
            stringResource(R.string.danmaku_color_default),
            stringResource(R.string.danmaku_color_colorful),
            stringResource(R.string.danmaku_color_gradient),
        ),
        selectedIndex = danmakuColorIndex(state.colorMode),
        onClick = { index ->
            onEvent(modeEvent(VodDanmakuSettingKeys.COLOR, state.tab, danmakuColorMode(index)))
        },
    )
    if (danmakuColorHintVisible(state.colorMode)) {
        Text(
            text = stringResource(R.string.danmaku_color_override_hint),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun TimingSettings(state: VodDanmakuSettingState, onEvent: (VodDanmakuSettingEvent) -> Unit) {
    SettingSlider(stringResource(R.string.danmaku_time_offset), state.timeOffsetMs, -300000f, 300000f, 1000f, danmakuMsLabel(snapOffset(state.timeOffsetMs, -300000f, 300000f, 1000f))) {
        onEvent(floatEvent(VodDanmakuSettingKeys.TIME_OFFSET, state.tab, it))
    }
    SettingSlider(stringResource(R.string.danmaku_duration), state.durationMs, 3000f, 15000f, 500f, danmakuMsLabel(snapOffset(state.durationMs, 3000f, 15000f, 500f))) {
        onEvent(floatEvent(VodDanmakuSettingKeys.DURATION, state.tab, it))
    }
    SettingSlider(stringResource(R.string.danmaku_fixed_duration), state.fixedDurationMs, 2000f, 10000f, 500f, danmakuMsLabel(snapOffset(state.fixedDurationMs, 2000f, 10000f, 500f))) {
        onEvent(floatEvent(VodDanmakuSettingKeys.FIXED_DURATION, state.tab, it))
    }
}

@Composable
private fun DensitySettings(state: VodDanmakuSettingState, onEvent: (VodDanmakuSettingEvent) -> Unit) {
    val auto = stringResource(R.string.danmaku_auto)
    SettingSlider(stringResource(R.string.danmaku_max_on_screen), state.maxOnScreen, 10f, 500f, 10f, snapOffset(state.maxOnScreen, 10f, 500f, 10f).roundToInt().toString()) {
        onEvent(floatEvent(VodDanmakuSettingKeys.MAX_ON_SCREEN, state.tab, it))
    }
    SettingSlider(stringResource(R.string.danmaku_scroll_area_ratio), state.scrollAreaRatio, 0.1f, 1f, 0.05f, danmakuDecimalLabel(snapOffset(state.scrollAreaRatio, 0.1f, 1f, 0.05f), "%.2f")) {
        onEvent(floatEvent(VodDanmakuSettingKeys.SCROLL_AREA, state.tab, it))
    }
    SettingSlider(stringResource(R.string.danmaku_scroll_gap_ratio), state.scrollGapRatio, 0f, 5f, 0.1f, danmakuDecimalLabel(snapOffset(state.scrollGapRatio, 0f, 5f, 0.1f), "%.1f")) {
        onEvent(floatEvent(VodDanmakuSettingKeys.SCROLL_GAP, state.tab, it))
    }
    SettingSlider(stringResource(R.string.danmaku_line_spacing), state.lineSpacing, 1f, 2f, 0.1f, danmakuDecimalLabel(snapOffset(state.lineSpacing, 1f, 2f, 0.1f), "%.1f")) {
        onEvent(floatEvent(VodDanmakuSettingKeys.LINE_SPACING, state.tab, it))
    }
    SettingSlider(stringResource(R.string.danmaku_max_scroll_lines), state.maxScrollLines, 0f, 20f, 1f, danmakuLinesLabel(snapOffset(state.maxScrollLines, 0f, 20f, 1f).roundToInt(), auto), state.showScroll) {
        onEvent(floatEvent(VodDanmakuSettingKeys.MAX_SCROLL, state.tab, it))
    }
    SettingSlider(stringResource(R.string.danmaku_max_top_lines), state.maxTopLines, 0f, 10f, 1f, danmakuLinesLabel(snapOffset(state.maxTopLines, 0f, 10f, 1f).roundToInt(), auto), state.showTop) {
        onEvent(floatEvent(VodDanmakuSettingKeys.MAX_TOP, state.tab, it))
    }
    SettingSlider(stringResource(R.string.danmaku_max_bottom_lines), state.maxBottomLines, 0f, 10f, 1f, danmakuLinesLabel(snapOffset(state.maxBottomLines, 0f, 10f, 1f).roundToInt(), auto), state.showBottom) {
        onEvent(floatEvent(VodDanmakuSettingKeys.MAX_BOTTOM, state.tab, it))
    }
}

@Composable
private fun DisplaySettings(state: VodDanmakuSettingState, onEvent: (VodDanmakuSettingEvent) -> Unit) {
    SettingSwitch(stringResource(R.string.danmaku_show_scroll), state.showScroll) {
        onEvent(boolEvent(VodDanmakuSettingKeys.SHOW_SCROLL, state.tab, it))
    }
    SettingSwitch(stringResource(R.string.danmaku_show_top), state.showTop) {
        onEvent(boolEvent(VodDanmakuSettingKeys.SHOW_TOP, state.tab, it))
    }
    SettingSwitch(stringResource(R.string.danmaku_show_bottom), state.showBottom) {
        onEvent(boolEvent(VodDanmakuSettingKeys.SHOW_BOTTOM, state.tab, it))
    }
    SettingSwitch(stringResource(R.string.danmaku_show_reverse), state.showReverse) {
        onEvent(boolEvent(VodDanmakuSettingKeys.SHOW_REVERSE, state.tab, it))
    }
    SettingSwitch(stringResource(R.string.danmaku_show_positioned), state.showPositioned) {
        onEvent(boolEvent(VodDanmakuSettingKeys.SHOW_POSITIONED, state.tab, it))
    }
    SettingSwitch(stringResource(R.string.danmaku_show_subtitle), state.showSubtitle) {
        onEvent(boolEvent(VodDanmakuSettingKeys.SHOW_SUBTITLE, state.tab, it))
    }
    SettingSwitch(stringResource(R.string.danmaku_show_special), state.showSpecial) {
        onEvent(boolEvent(VodDanmakuSettingKeys.SHOW_SPECIAL, state.tab, it))
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color.White,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.White.copy(alpha = 0.24f),
            ),
        )
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    from: Float,
    to: Float,
    step: Float,
    valueText: String,
    enabled: Boolean = true,
    onChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
            .padding(horizontal = 24.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(valueText, color = Color.White, fontSize = 14.sp)
        }
        Slider(
            value = snapOffset(value, from, to, step),
            onValueChange = { onChange(snapOffset(it, from, to, step)) },
            valueRange = from..to,
            steps = sliderSteps(from, to, step),
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.24f),
                disabledThumbColor = Color.White.copy(alpha = 0.38f),
                disabledActiveTrackColor = Color.White.copy(alpha = 0.38f),
                disabledInactiveTrackColor = Color.White.copy(alpha = 0.12f),
            ),
        )
    }
}

private fun boolEvent(key: Int, tab: Int, value: Boolean): VodDanmakuSettingEvent {
    return VodDanmakuSettingEvent(VodDanmakuSettingIds.BOOL, key = key, tab = tab, boolValue = value)
}

private fun floatEvent(key: Int, tab: Int, value: Float): VodDanmakuSettingEvent {
    return VodDanmakuSettingEvent(VodDanmakuSettingIds.FLOAT, key = key, tab = tab, floatValue = value)
}

private fun modeEvent(key: Int, tab: Int, value: Int): VodDanmakuSettingEvent {
    return VodDanmakuSettingEvent(VodDanmakuSettingIds.MODE, key = key, tab = tab, intValue = value)
}
