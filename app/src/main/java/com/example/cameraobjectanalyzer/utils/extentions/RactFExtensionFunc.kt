package com.example.cameraobjectanalyzer.utils.extentions

import android.graphics.RectF


/*
*   Model input:      1280 x 1280   (square, YOLO)
    ImageProxy:       1280 x 720    (camera frame, 16:9)
    PreviewView:      1080 x 1080   (your UI view)
    Canvas:           1080 x 2294   (full screen overlay)
* */
fun RectF.mapToPreview(): RectF {
    val padY = 280f

    // Step 1: remove YOLO padding
    val noPad = RectF(
        left,
        top - padY,
        right,
        bottom - padY
    )

    // Step 2: scale to PreviewView
    val scale = 1080f / 720f  // 1.5

    val scaled = RectF(
        noPad.left * scale,
        noPad.top * scale,
        noPad.right * scale,
        noPad.bottom * scale
    )

    // Step 3: apply horizontal crop offset
    val cropX = (1280f * scale - 1080f) / 2f  // 420

    return RectF(
        scaled.left - cropX,
        scaled.top,
        scaled.right - cropX,
        scaled.bottom
    )
}