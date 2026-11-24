package org.feeluown.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap

/**
 * 桌面端暂未接入相册选图：设置页会把入口显示为不可用，功能开关保持关闭。
 */
@Composable
internal actual fun rememberBackgroundImagePicker(
    onPicked: (String?) -> Unit,
): BackgroundImagePicker = remember {
    BackgroundImagePicker(isAvailable = false) {}
}

@Composable
internal actual fun rememberBackgroundImageBitmap(
    path: String?,
    maxDimensionPx: Int,
): ImageBitmap? = null
