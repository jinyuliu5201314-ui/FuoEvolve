package org.feeluown.mobile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/** 自定义页面背景在渲染时使用的解码尺寸上限（像素），控制常驻内存占用。 */
internal const val PAGE_BACKGROUND_MAX_DIMENSION_PX = 1600

/**
 * 相册选图入口。
 *
 * [isAvailable] 为 false 表示当前平台没有接入选图能力，设置页应展示为不可用。
 */
internal class BackgroundImagePicker(
    val isAvailable: Boolean,
    val launch: () -> Unit,
)

/**
 * 让用户从系统相册挑一张图片作为页面背景。
 *
 * 回调参数是应用私有目录内的图片绝对路径；用户取消或读取失败时为 null，
 * 调用方应保留原有背景设置。
 */
@Composable
internal expect fun rememberBackgroundImagePicker(onPicked: (String?) -> Unit): BackgroundImagePicker

/** 解码 [path] 指向的本地背景图；路径为空、文件缺失或平台不支持时返回 null。 */
@Composable
internal expect fun rememberBackgroundImageBitmap(
    path: String?,
    maxDimensionPx: Int = PAGE_BACKGROUND_MAX_DIMENSION_PX,
): ImageBitmap?

/**
 * 铺满整个窗口的自定义背景：底图 + 主题色遮罩。
 *
 * 遮罩用 surface 色而不是纯黑，亮色和暗色主题下都能保持内容可读。
 */
@Composable
internal fun PageBackgroundImage(
    bitmap: ImageBitmap,
    settings: BackgroundImageSettings,
) {
    Box(Modifier.fillMaxSize()) {
        val imageModifier = if (settings.blurDp > 0f) {
            Modifier.fillMaxSize().blur(settings.blurDp.dp)
        } else {
            Modifier.fillMaxSize()
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = imageModifier,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = settings.dim)),
        )
    }
}
