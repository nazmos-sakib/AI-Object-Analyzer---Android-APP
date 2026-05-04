package com.example.cameraobjectanalyzer.domain.model

import android.graphics.RectF

data class Detection(
    val bbox: List<Float>,
    val `class`: String,
    val confidence: Float
){
    override fun toString(): String {
        return "Detection(" +
                "label='$`class`, " +
                "confidence=${"%.2f".format(confidence)}, " +
                "boundingBox=[left=${bbox[0]}, top=${bbox[1]}, right=${bbox[2]}, bottom=${bbox[3]}]" +
                ")"
    }
}

fun Detection.toRectFOrNull(): RectF? {
    if (bbox.size < 4) return null

    return RectF(
        bbox[0],
        bbox[1],
        bbox[2],
        bbox[3]
    )
}

fun Detection.toRectF(): RectF {
    return RectF(
        bbox[0], // left
        bbox[1], // top
        bbox[2], // right
        bbox[3]  // bottom
    )
}