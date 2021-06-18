package com.lucas.gl.utils

import android.graphics.Bitmap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by lucas on 2021/6/18.
 */
object BitmapUtil {

    fun save(bitmap: Bitmap?, format: Bitmap.CompressFormat?, quality: Int, destFile: File): String? {
        try {
            val out = FileOutputStream(destFile)
            if (bitmap!!.compress(format, quality, out)) {
                out.flush()
                out.close()
            }
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            return destFile.absolutePath
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}