package com.apc.smartinstallation.util

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun Bitmap.toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun Bitmap.similarTo(other: Bitmap, threshold: Double = 0.9): Boolean {
        if (this.width != other.width || this.height != other.height) {
            return false
        }

        val pixels1 = IntArray(this.width * this.height)
        val pixels2 = IntArray(other.width * other.height)

        this.getPixels(pixels1, 0, this.width, 0, 0, this.width, this.height)
        other.getPixels(pixels2, 0, other.width, 0, 0, other.width, other.height)

        var similarPixels = 0
        for (i in pixels1.indices) {
            if (pixels1[i] == pixels2[i]) {
                similarPixels++
            }
        }

        val similarity = similarPixels.toDouble() / pixels1.size
        return similarity >= threshold
    }
}