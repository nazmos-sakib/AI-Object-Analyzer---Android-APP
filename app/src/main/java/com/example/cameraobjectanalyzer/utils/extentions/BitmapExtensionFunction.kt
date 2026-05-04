package com.example.cameraobjectanalyzer.utils.extentions

import android.graphics.Bitmap
import android.graphics.Matrix
import java.io.ByteArrayOutputStream

fun Bitmap.rotateBitmap(rotation: Int): Bitmap {
    val matrix = Matrix().apply {
        postRotate(rotation.toFloat())
    }
    return Bitmap.createBitmap(
        this, 0, 0,
        width, height,
        matrix, true
    )
}

fun Bitmap.bitmapToJpegBytes( ): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 90, stream)
    return stream.toByteArray()
}