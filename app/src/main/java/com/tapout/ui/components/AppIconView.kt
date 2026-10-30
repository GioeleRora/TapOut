package com.tapout.ui.components

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tapout.ui.theme.ElectricPurple
import com.tapout.ui.theme.SurfaceDark
import com.tapout.ui.theme.SurfaceElevated
import com.tapout.ui.theme.TextMuted
import com.tapout.ui.theme.TextWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AppIconCache {
    private val memoryCache = LruCache<String, ImageBitmap>(300)

    fun get(packageName: String): ImageBitmap? = memoryCache.get(packageName)

    fun put(packageName: String, bitmap: ImageBitmap) {
        memoryCache.put(packageName, bitmap)
    }

    suspend fun loadIcon(context: Context, packageName: String): ImageBitmap? = withContext(Dispatchers.IO) {
        get(packageName)?.let { return@withContext it }

        try {
            val pm = context.packageManager
            val drawable = pm.getApplicationIcon(packageName)
            val bitmap = drawableToBitmap(drawable)
            val imageBitmap = bitmap.asImageBitmap()
            put(packageName, imageBitmap)
            imageBitmap
        } catch (_: Exception) {
            null
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}

@Composable
fun AppIcon(
    packageName: String,
    appName: String = "",
    modifier: Modifier = Modifier,
    size: Dp = 38.dp
) {
    val context = LocalContext.current
    var iconBitmap by remember(packageName) { mutableStateOf(AppIconCache.get(packageName)) }

    LaunchedEffect(packageName) {
        if (iconBitmap == null) {
            val loaded = AppIconCache.loadIcon(context, packageName)
            iconBitmap = loaded
        }
    }

    val currentBitmap = iconBitmap
    if (currentBitmap != null) {
        Image(
            bitmap = currentBitmap,
            contentDescription = appName,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.22f))
        )
    } else {
        // Fallback pulito: monogramma con iniziale o icona app
        val initial = appName.trim().firstOrNull()?.uppercase() ?: ""
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.22f))
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            if (initial.isNotBlank() && initial.first().isLetterOrDigit()) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricPurple,
                        fontSize = (size.value * 0.45f).sp
                    )
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = appName,
                    tint = TextMuted,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }
    }
}
