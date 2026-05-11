package com.example.cameraobjectanalyzer.presentation

// presentation/ui/CameraScreen.kt

import android.graphics.Paint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cameraobjectanalyzer.utils.Constants.UiDebugTag
import com.example.cameraobjectanalyzer.domain.model.Detection
import com.example.cameraobjectanalyzer.domain.model.toRectF
import com.example.cameraobjectanalyzer.presentation.viewmodel.CameraViewModel
import com.example.cameraobjectanalyzer.utils.ChoreographerFPSMonitor
import com.example.cameraobjectanalyzer.utils.Constants.ImageDebugTag
import com.example.cameraobjectanalyzer.utils.Constants.PerformanceDebugTag
import com.example.cameraobjectanalyzer.utils.extentions.bitmapToJpegBytes
import com.example.cameraobjectanalyzer.utils.extentions.mapToPreview
import com.example.cameraobjectanalyzer.utils.extentions.rgbToBitmap
import com.example.cameraobjectanalyzer.utils.extentions.rotateBitmap
import com.example.cameraobjectanalyzer.utils.extentions.yuvToJpegByte
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(viewModel: CameraViewModel) {

    val detectedObjects by viewModel.detectedObjects.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val previewViewFps by viewModel.previewViewFps.collectAsStateWithLifecycle()
    val imageProxyFps by viewModel.imageProxyFps.collectAsStateWithLifecycle()
    val inferenceFps by viewModel.inferenceFps.collectAsStateWithLifecycle()

    var previewViewSize: Size by remember { mutableStateOf(Size(0f,0f)) }
    var imageProxySize: Size by remember { mutableStateOf(Size(0f,0f)) }
    /*
    * output imageProxy Format
    * ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888
    * ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
    * hard code it or check before using it
    * if (imgProxy.format == PixelFormat.RGBA_8888) {
                    val buffer = imgProxy.planes[0].buffer
                    // buffer contains RGBA pixel data
                }
    * */
    //val outputImageProxyFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888
    val outputImageProxyFormat = ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888

    val context = LocalContext.current

    //================Preview FPS monitor
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    ChoreographerFPSMonitor.startFpsMonitor(viewModel)
                }

                Lifecycle.Event.ON_PAUSE -> {
                    ChoreographerFPSMonitor.stopFpsMonitor()
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            ChoreographerFPSMonitor.stopFpsMonitor()
        }
    }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {

        CameraPreview(
            outputImageProxyFormat = outputImageProxyFormat,
            onFrame = {_imgProxy,_previewView->
                viewModel.updateImageProxyFPS()
                //for proper bounding box =========================
                previewViewSize = Size(_previewView .width.toFloat(), _previewView.height.toFloat())
                imageProxySize = if (_imgProxy.imageInfo.rotationDegrees in intArrayOf(0,180)) {
                    Size(_imgProxy.width.toFloat(), _imgProxy.height.toFloat())
                }
                else {Size(_imgProxy.height.toFloat(), _imgProxy.width.toFloat())}
                //end bounding box pre calculation =========================

                //skip imageProxy if its already in process
                if(!viewModel.isProcessing.value){

                    val now = System.currentTimeMillis()

                    //viewModel.debugInfo(_imgProxy,_previewView)
                    //val jpegBytes = _imgProxy.yuvToJpegByte(quality = 70)
                    val rotatedBitMap = _imgProxy.rgbToBitmap().rotateBitmap(_imgProxy.imageInfo.rotationDegrees)
                    Log.d(PerformanceDebugTag, "CameraScreen: Time to prepare image: ${System.currentTimeMillis()-now}")

                    //API call
                    viewModel.uploadImageV2(
                        rotatedBitMap.bitmapToJpegBytes()
                    )

                    //save image in local memory
                    /*scope.launch(Dispatchers.IO) {
                        //if (viewModel.isProcessing.value) return@launch
                        //viewModel.saveImageProxy(context,jpegBytes,_imgProxy.imageInfo.rotationDegrees.toFloat())
                        viewModel.saveImageProxy(context,rotatedBitMap)
                    }*/

                    //viewModel.testUploadImage(jpegBytes)


                    //viewModel.viewModelFPSRateLimit()

                    //Log.d(PerformanceDebugTag, "CameraScreen: Time to process: ${System.currentTimeMillis()-now}")
                }



            },
            onDetections = {

            }
        )
        // Overlay canvas for bounding boxes
        Canvas(modifier = Modifier
            //.matchParentSize()
            .fillMaxSize()
        ) {
            //if previewView is set to fillMaxSize without an aspect ratio then both will have -> width:1080 height: 2294
            detectedObjects.forEach { detection ->
                //Log.d(UiDebugTag, "CameraScreen: $detection")
                drawBoundingBox(
                    detection = detection,
                    canvasSize = size,
                    previewViewSize = previewViewSize,
                    imageProxySize = imageProxySize
                )
            }
        }

        // FPS and processing indicator
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Text(
                text = "PreviewView FPS: ${"%.1f".format(previewViewFps)}",
                modifier = Modifier
                    //.align(Alignment.TopStart)
                    .padding(16.dp,8.dp),
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "ImageProxy FPS: ${"%.1f".format(imageProxyFps)}",
                modifier = Modifier
                    //.align(Alignment.TopStart)
                    .padding(16.dp,8.dp),
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "Inference FPS: ${"%.1f".format(inferenceFps)}",
                modifier = Modifier
                    //.align(Alignment.TopStart)
                    .padding(16.dp,8.dp),
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

//extension function
private fun DrawScope.drawBoundingBox(
    detection: Detection,
    canvasSize: Size,
    previewViewSize: Size,
    imageProxySize: Size,
) {
    if (previewViewSize.width == 0f || previewViewSize.height == 0f) return

    //tune the canvas size and previewView size. and set an offset
    // devided by 2 as preview is in center and top and bottom has the same offset
    val previewTopOffset = (canvasSize.height - previewViewSize.height) / 2f

    val bbox = detection.toRectF()
        .mapToPreview( previewViewSize, imageProxySize)
        .apply {
            offset(0f, previewTopOffset)
        }

    //Log.d(ImageDebugTag, "drawBoundingBox: ract: $bbox")



    val left  = bbox.left
    val top = bbox.top
    val right = bbox.right
    val bottom = bbox.bottom



    // Draw rectangle
    drawRect(
        color = Color.Green,
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
        style = Stroke(width = 4f)
    )

    // Draw label background
   val labelText = "${detection.`class`} ${"%.1f".format(detection.confidence * 100)}%"
    /*     val textSize = 14.sp
        val textPaint =  TextPainter(
            text = androidx.compose.ui.text.AnnotatedString(labelText),
            textAlign = androidx.compose.ui.text.style.TextAlign.Left,
            density = androidx.compose.ui.unit.Density(1f, 1f, 1f),
            layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr
        )*/

    // Draw label
    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint().apply {
            color = android.graphics.Color.GREEN
            textSize = 24f
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        drawText(
            labelText,
            left + ((right-left)/2),
            top - 5,
            paint
        )
    }
}

