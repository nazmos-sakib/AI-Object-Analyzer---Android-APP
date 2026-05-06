package com.example.cameraobjectanalyzer.utils.extentions

import android.graphics.RectF
import android.util.Log.d
import androidx.compose.ui.geometry.Size
import com.example.cameraobjectanalyzer.utils.Constants.ImageDebugTag
import kotlin.math.max


/*
*   Model input:      1280 x 1280 (640x640)  (square, YOLO)
    ImageProxy:       1280 x 720    (camera frame, 16:9)
    PreviewView:      1080 x 1080   (your UI view)
    Canvas:           1080 x 2294   (full screen overlay)
* */

fun RectF.mapToPreview(
    previewViewSize: Size,
    imageProxySize: Size,
): RectF {


    val scale = max(
        previewViewSize.height/imageProxySize.height,
        previewViewSize.width/imageProxySize.width
    )

    val scaledWidth = imageProxySize.width * scale
    val scaledHeight = imageProxySize.height * scale

    //Offset (crop happens horizontally)
    val dx = (previewViewSize.width - scaledWidth) / 2f
    val dy = (previewViewSize.height - scaledHeight) / 2f


    //logs
    /*d(ImageDebugTag, "mapToPreviewV2: previewViewSize height: ${previewViewSize.height}")
    d(ImageDebugTag, "mapToPreviewV2: previewViewSize width: ${previewViewSize.width}")
    d(ImageDebugTag, "mapToPreviewV2: imageProxySize h: ${imageProxySize.height}")
    d(ImageDebugTag, "mapToPreviewV2: imageProxySize w: ${imageProxySize.width}")

    d(ImageDebugTag, "mapToPreviewV2: scale: $scale")
    d(ImageDebugTag, "mapToPreviewV2: dx: $dx")
    d(ImageDebugTag, "mapToPreviewV2: dy: $dy")*/

    return RectF(
        left * scale + dx,
        top * scale + dy,
        right * scale + dx,
        bottom * scale + dy
    )
}