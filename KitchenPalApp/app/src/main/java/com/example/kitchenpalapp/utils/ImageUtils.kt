package com.example.kitchenpalapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream

object ImageUtils {

    /**
     * 把 Uri 指向的图片统一解码、限制最大边长并压缩成 JPEG 字节，便于上传识别。
     * 兼容 API 24（BitmapFactory 采样）与 API 28+（ImageDecoder，支持 HEIF/WebP）。
     */
    fun uriToJpegBytes(context: Context, uri: Uri, maxDim: Int = 1280, quality: Int = 85): ByteArray? {
        return try {
            val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val scale = maxDim.toFloat() / maxOf(info.size.width, info.size.height)
                    if (scale < 1f) {
                        decoder.setTargetSize(
                            (info.size.width * scale).toInt().coerceAtLeast(1),
                            (info.size.height * scale).toInt().coerceAtLeast(1)
                        )
                    }
                }
            } else {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                val sample = maxOf(1, maxOf(bounds.outWidth, bounds.outHeight) / maxDim)
                val opts = BitmapFactory.Options().apply { inSampleSize = sample }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opts)
                }
            }
            if (bitmap == null) return null
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            baos.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}
