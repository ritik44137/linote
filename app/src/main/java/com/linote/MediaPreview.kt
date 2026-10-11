package com.linote

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.util.LruCache
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linote.editor.MediaKind
import com.linote.editor.Piece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

sealed interface LoadedPreview {
    data class Picture(val image: ImageBitmap, val aspect: Float) : LoadedPreview
    data object Icon : LoadedPreview
}

@Composable
fun MediaTile(piece: Piece.Media, modifier: Modifier = Modifier) {
    val loaded = rememberPreview(piece)
    val ink = colorResource(R.color.ink)
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val preview = loaded) {
            is LoadedPreview.Picture -> Image(
                preview.image,
                contentDescription = piece.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            LoadedPreview.Icon -> TypeIcon(piece.name, 32.dp, ink)
        }
    }
}

@Composable
fun SoloMedia(piece: Piece.Media, modifier: Modifier = Modifier) {
    when (val preview = rememberPreview(piece)) {
        is LoadedPreview.Picture -> Image(
            preview.image,
            contentDescription = piece.name,
            modifier = modifier.fillMaxWidth().aspectRatio(preview.aspect),
            contentScale = ContentScale.Fit,
        )
        LoadedPreview.Icon -> FileFallback(piece.name, modifier)
    }
}

@Composable
fun FileFallback(name: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    val ink = colorResource(R.color.ink)
    Row(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .border(1.dp, colorResource(R.color.rule), shape)
            .background(colorResource(R.color.paper), shape)
            .padding(start = 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TypeIcon(name, 24.dp, ink)
        Spacer(Modifier.width(8.dp))
        BasicText(
            name,
            style = TextStyle(color = ink, fontFamily = FontFamily.SansSerif, fontSize = 15.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TypeIcon(name: String, size: Dp, tint: androidx.compose.ui.graphics.Color) {
    val context = LocalContext.current
    val icon = remember(name) { cachedTypeIcon(context, name) }
    if (icon != null) {
        Image(icon, contentDescription = null, modifier = Modifier.size(size))
    } else {
        FileGlyph(Modifier.size(size), tint, size)
    }
}

@Composable
private fun rememberPreview(piece: Piece.Media): LoadedPreview {
    val context = LocalContext.current.applicationContext
    val key = previewKey(piece)
    val state = produceState(
        remember(key) { previewBitmaps.get(key)?.let { picture(it) } ?: LoadedPreview.Icon },
        key,
    ) {
        previewBitmaps.get(key)?.let {
            value = picture(it)
            return@produceState
        }
        value = withContext(Dispatchers.IO) { loadPreview(context, piece) }
    }
    return state.value
}

private const val PREVIEW_EDGE = 1080

private val previewBitmaps = object : LruCache<String, Bitmap>(12 * 1024 * 1024) {
    override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount.coerceAtLeast(1)
}

private val typeIcons = HashMap<String, ImageBitmap?>()

private fun previewKey(piece: Piece.Media) = "${piece.kind}\u0000${piece.source}\u0000${piece.name}"

private fun picture(bitmap: Bitmap): LoadedPreview {
    val aspect = if (bitmap.height == 0) 1f else bitmap.width.toFloat() / bitmap.height
    return LoadedPreview.Picture(bitmap.asImageBitmap(), aspect)
}

private fun loadPreview(context: android.content.Context, piece: Piece.Media): LoadedPreview {
    val key = previewKey(piece)
    previewBitmaps.get(key)?.let { return picture(it) }
    val bitmap = try {
        when (piece.kind) {
            MediaKind.IMAGE -> decodeImage(context, piece.source)
            MediaKind.VIDEO -> decodeFrame(context, piece.source)?.let { scaleDown(it) }
            MediaKind.FILE -> when {
                isImageName(piece.name) -> decodeImage(context, piece.source)
                isPdf(piece.name) -> decodePdf(context, piece.source)
                else -> null
            }
        }
    } catch (e: Exception) {
        null
    } ?: return LoadedPreview.Icon
    previewBitmaps.put(key, bitmap)
    return picture(bitmap)
}

private fun scaleDown(bitmap: Bitmap): Bitmap {
    val edge = max(bitmap.width, bitmap.height)
    if (edge <= PREVIEW_EDGE) return bitmap
    return try {
        val scale = PREVIEW_EDGE.toFloat() / edge
        val w = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
        val h = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)
        if (scaled != bitmap) bitmap.recycle()
        scaled
    } catch (e: Exception) {
        bitmap
    }
}

private fun decodeImage(context: android.content.Context, source: String): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    open(context, source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / sample > PREVIEW_EDGE || bounds.outHeight / sample > PREVIEW_EDGE) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return open(context, source)?.use { BitmapFactory.decodeStream(it, null, options) }
}

private fun decodeFrame(context: android.content.Context, source: String): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, Uri.parse(source))
        retriever.getFrameAtTime(0)
    } finally {
        retriever.release()
    }
}

private fun decodePdf(context: android.content.Context, source: String): Bitmap? {
    val pfd = context.contentResolver.openFileDescriptor(Uri.parse(source), "r") ?: return null
    return pfd.use {
        PdfRenderer(it).use { renderer ->
            if (renderer.pageCount == 0) return null
            renderer.openPage(0).use { page ->
                val edge = max(page.width, page.height).coerceAtLeast(1)
                val scale = min(1f, PREVIEW_EDGE.toFloat() / edge)
                val w = (page.width * scale).roundToInt().coerceAtLeast(1)
                val h = (page.height * scale).roundToInt().coerceAtLeast(1)
                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
        }
    }
}

private fun open(context: android.content.Context, source: String) =
    context.contentResolver.openInputStream(Uri.parse(source))

private fun isImageName(name: String): Boolean {
    val mime = mime(name) ?: return false
    return mime.startsWith("image/")
}

private fun isPdf(name: String): Boolean =
    name.endsWith(".pdf", ignoreCase = true) || mime(name) == "application/pdf"

private fun mime(name: String): String? {
    val ext = name.substringAfterLast('.', "")
    if (ext.isEmpty() || ext == name) return null
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase())
}

private fun cachedTypeIcon(context: android.content.Context, name: String): ImageBitmap? {
    val type = mime(name) ?: return null
    synchronized(typeIcons) {
        if (typeIcons.containsKey(type)) return typeIcons[type]
    }
    val icon = systemIcon(context, name)?.toImageBitmap()
    synchronized(typeIcons) { typeIcons[type] = icon }
    return icon
}

private fun systemIcon(context: android.content.Context, name: String): android.graphics.drawable.Drawable? {
    val type = mime(name) ?: return null
    val resolved = context.packageManager.resolveActivity(
        Intent(Intent.ACTION_VIEW).setType(type),
        PackageManager.MATCH_DEFAULT_ONLY,
    ) ?: return null
    return resolved.loadIcon(context.packageManager)
}

private fun android.graphics.drawable.Drawable.toImageBitmap(): ImageBitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth.coerceAtLeast(1), intrinsicHeight.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap.asImageBitmap()
}
