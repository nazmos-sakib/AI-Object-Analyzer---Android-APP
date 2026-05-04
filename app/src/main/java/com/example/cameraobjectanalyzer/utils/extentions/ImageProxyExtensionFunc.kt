package com.example.cameraobjectanalyzer.utils.extentions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import androidx.core.graphics.createBitmap

/**
 * Extension function to convert ImageProxy
 */

fun ImageProxy.rgbToByte(): ByteArray {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return bytes
}

fun ImageProxy.yuvToJpegByte( quality: Int = 80): ByteArray {
    val yBuffer =  planes[0].buffer
    val uBuffer =  planes[1].buffer
    val vBuffer =  planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(
        nv21,
        ImageFormat.NV21,
        width,
        height,
        null
    )

    val outputStream = ByteArrayOutputStream()

    yuvImage.compressToJpeg(
        Rect(0, 0, width, height),
        quality,
        outputStream
    )

    return outputStream.toByteArray()
}

fun ImageProxy.byteArrayToBitmap(): Bitmap{
    //val jpegBytes = yuvToJpegByte(100)
    val jpegBytes = rgbToByte()
    return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
}

fun ImageProxy.rgbaToBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bitmap = createBitmap(width, height)
    bitmap.copyPixelsFromBuffer(buffer)
    return bitmap
}