package org.feeluown.mobile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val BACKGROUND_LOG_TAG = "BackgroundImage"
private const val BACKGROUND_IMAGE_DIRECTORY = "background"
private const val BACKGROUND_IMAGE_FILE_NAME = "page_background.jpg"
private const val BACKGROUND_IMAGE_TEMP_FILE_NAME = "page_background.jpg.tmp"
private const val BACKGROUND_IMAGE_STORED_MAX_DIMENSION = 2400
private const val BACKGROUND_IMAGE_JPEG_QUALITY = 90

@Composable
internal actual fun rememberBackgroundImagePicker(
    onPicked: (String?) -> Unit,
): BackgroundImagePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnPicked by rememberUpdatedState(onPicked)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) {
            // 用户在相册里取消，保留原有背景。
            currentOnPicked(null)
        } else {
            scope.launch {
                val storedPath = withContext(Dispatchers.IO) {
                    runCatching { importPickedBackgroundImage(context, uri) }
                        .onFailure { error ->
                            AppLogger.w(
                                BACKGROUND_LOG_TAG,
                                "import failed stage=io error=${error.javaClass.simpleName}",
                            )
                        }
                        .getOrNull()
                }
                currentOnPicked(storedPath)
            }
        }
    }
    return remember(picker) {
        BackgroundImagePicker(isAvailable = true) {
            runCatching {
                picker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }.onFailure { error ->
                AppLogger.w(
                    BACKGROUND_LOG_TAG,
                    "picker launch failed error=${error.javaClass.simpleName}",
                )
            }
        }
    }
}

@Composable
internal actual fun rememberBackgroundImageBitmap(
    path: String?,
    maxDimensionPx: Int,
): ImageBitmap? {
    val normalizedPath = path?.takeIf { it.isNotBlank() }
    var image by remember(normalizedPath, maxDimensionPx) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(normalizedPath, maxDimensionPx) {
        image = normalizedPath?.let { filePath ->
            withContext(Dispatchers.IO) {
                runCatching {
                    decodeSampledFile(File(filePath), maxDimensionPx)?.asImageBitmap()
                }.onFailure { error ->
                    AppLogger.w(
                        BACKGROUND_LOG_TAG,
                        "decode failed path=${File(filePath).name} error=${error.javaClass.simpleName}",
                    )
                }.getOrNull()
            }
        }
    }
    return image
}

/**
 * 把相册里的图片解码、缩放后写入应用私有目录，返回可长期使用的绝对路径。
 *
 * 相册 Uri 的读权限是临时的，直接持久化 Uri 会在重启后失效，所以这里复制一份副本。
 */
private fun importPickedBackgroundImage(context: Context, uri: Uri): String? {
    val decoded = decodePickedBitmap(context, uri) ?: return null
    val directory = File(context.filesDir, BACKGROUND_IMAGE_DIRECTORY)
    if (!directory.exists() && !directory.mkdirs()) {
        decoded.recycle()
        return null
    }
    val target = File(directory, BACKGROUND_IMAGE_FILE_NAME)
    val temp = File(directory, BACKGROUND_IMAGE_TEMP_FILE_NAME)
    val written = runCatching {
        temp.outputStream().use { stream ->
            decoded.compress(Bitmap.CompressFormat.JPEG, BACKGROUND_IMAGE_JPEG_QUALITY, stream)
        }
    }.getOrDefault(false)
    decoded.recycle()
    if (!written) {
        temp.delete()
        return null
    }
    if (!temp.renameTo(target)) {
        runCatching { temp.copyTo(target, overwrite = true) }.getOrNull() ?: return null
        temp.delete()
    }
    return target.absolutePath
}

private fun decodePickedBitmap(context: Context, uri: Uri): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
    }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply {
        inSampleSize = backgroundSampleSize(bounds.outWidth, bounds.outHeight)
    }
    val decoded = runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }.getOrNull() ?: return null

    val rotation = runCatching {
        context.contentResolver.openInputStream(uri)?.use { readExifRotation(it) } ?: 0
    }.getOrDefault(0)
    return if (rotation == 0) decoded else rotateBitmap(decoded, rotation)
}

private fun backgroundSampleSize(width: Int, height: Int): Int {
    var sample = 1
    val longestEdge = maxOf(width, height)
    while (longestEdge / (sample * 2) >= BACKGROUND_IMAGE_STORED_MAX_DIMENSION) {
        sample *= 2
    }
    return sample
}

private fun readExifRotation(stream: InputStream): Int {
    val orientation = runCatching {
        ExifInterface(stream).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    val rotated = runCatching {
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }.getOrNull() ?: return bitmap
    if (rotated !== bitmap) bitmap.recycle()
    return rotated
}

private fun decodeSampledFile(file: File, maxDimensionPx: Int): Bitmap? {
    if (!file.isFile) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val limit = maxDimensionPx.coerceAtLeast(1)
    var sample = 1
    val longestEdge = maxOf(bounds.outWidth, bounds.outHeight)
    while (longestEdge / (sample * 2) >= limit) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(file.absolutePath, options)
}
