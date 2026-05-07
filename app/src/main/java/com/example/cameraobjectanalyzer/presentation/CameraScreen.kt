package com.example.cameraobjectanalyzer.presentation

// presentation/ui/CameraScreen.kt

import android.graphics.Paint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.sync.Mutex

@Composable
fun CameraScreen(viewModel: CameraViewModel) {

    val serverBitmap by viewModel.serverBitmap.collectAsStateWithLifecycle()
    val hasReceivedFirstFrame by viewModel.hasReceivedFirstFrame.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val fps by viewModel.fps.collectAsStateWithLifecycle()
    val cameraFps by viewModel.cameraFps.collectAsStateWithLifecycle()

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
                modifier = Modifier.alpha(
                    if (hasReceivedFirstFrame) 0f else 1f
                ),
                outputImageProxyFormat = outputImageProxyFormat,
                onFrame = { _imgProxy, _previewView ->

                    //for proper bounding box =========================
                    previewViewSize =
                        Size(_previewView.width.toFloat(), _previewView.height.toFloat())
                    imageProxySize =
                        if (_imgProxy.imageInfo.rotationDegrees in intArrayOf(0, 180)) {
                            Size(_imgProxy.width.toFloat(), _imgProxy.height.toFloat())
                        } else {
                            Size(_imgProxy.height.toFloat(), _imgProxy.width.toFloat())
                        }
                    //end bounding box pre calculation =========================

                    //skip imageProxy if its already in process
                    if (!viewModel.isProcessing.value) {

                        val now = System.currentTimeMillis()

                        //viewModel.debugInfo(_imgProxy, _previewView)
                        //val jpegBytes = _imgProxy.yuvToJpegByte(quality = 70)
                        val rotatedBitMap = _imgProxy.rgbToBitmap()
                            .rotateBitmap(_imgProxy.imageInfo.rotationDegrees)
                        Log.d(
                            PerformanceDebugTag,
                            "CameraScreen: Time to prepare image: ${System.currentTimeMillis() - now}"
                        )

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

                        //Log.d(PerformanceDebugTag, "CameraScreen: Time to process: ${System.currentTimeMillis()-now}")
                    }


                }
            )

        // SERVER RENDERED IMAGE
        serverBitmap?.let { bitmap ->

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

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
                text = "FPS: ${"%.1f".format(fps)}",
                modifier = Modifier
                    //.align(Alignment.TopStart)
                    .padding(16.dp),
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "Camera FPS: ${"%.1f".format(cameraFps)}",
                modifier = Modifier
                    //.align(Alignment.TopStart)
                    .padding(16.dp),
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}
