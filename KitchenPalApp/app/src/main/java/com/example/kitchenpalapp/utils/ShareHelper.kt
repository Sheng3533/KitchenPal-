package com.example.kitchenpalapp.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

object ShareHelper {
    fun shareRecipe(context: Context, title: String, description: String) {
        val shareText = "来看看这个菜谱：$title\n\n$description\n\n下载 KitchenPal 一起烹饪吧！"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "分享菜谱"))
    }

    fun shareDietReport(context: Context, reportTitle: String, reportContent: String) {
        val shareText = "$reportTitle\n\n$reportContent\n\n下载 KitchenPal 查看你的饮食报告！"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, reportTitle)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "分享饮食报告"))
    }

    fun shareApp(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.example.kitchenpalapp")
        }
        context.startActivity(intent)
    }

    /**
     * 把位图保存到系统相册。
     *
     * - Android 10（API 29）及以上：走 MediaStore，存到 Pictures/KitchenPal，无需任何权限。
     * - Android 9 及以下：MediaStore 写入需要 WRITE_EXTERNAL_STORAGE，本应用不申请该权限，
     *   因此退化为写入应用自己的外部目录（无需权限，不会崩溃），路径仍可用。
     *
     * @return 是否保存成功
     */
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, displayName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/KitchenPal"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return false
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                } ?: return false
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                true
            } else {
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    ?: context.filesDir
                if (!dir.exists()) dir.mkdirs()
                FileOutputStream(File(dir, displayName)).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}