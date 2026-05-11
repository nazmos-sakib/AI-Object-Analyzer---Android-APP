package com.example.cameraobjectanalyzer.presentation.viewmodel

// presentation/viewmodel/CameraViewModel.kt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cameraobjectanalyzer.utils.Constants.PerformanceDebugTag
import com.example.cameraobjectanalyzer.domain.model.Detection
import com.example.cameraobjectanalyzer.domain.model.DetectionResponse
import com.example.cameraobjectanalyzer.utils.Constants.BASE_URL
import com.example.cameraobjectanalyzer.utils.Constants.ImageDebugTag
import com.example.cameraobjectanalyzer.utils.Constants.NetworkDebugTag
import com.example.cameraobjectanalyzer.utils.FpsCounter
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dispatcher
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.text.toFloat


class CameraViewModel(

) : ViewModel() {


    private val gson = Gson()
    private val inferenceFpsCounter = FpsCounter()
    private val imageProxyFpsCounter = FpsCounter()

/*
    private val _detectedObjects = MutableStateFlow<List<DetectedObject>>(emptyList())
    val detectedObjects: StateFlow<List<DetectedObject>> = _detectedObjects.asStateFlow()
*/

    private val _detectedObjects = MutableStateFlow<List<Detection>>(emptyList())
    val detectedObjects: StateFlow<List<Detection>> = _detectedObjects.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _imageProxyFps = MutableStateFlow(0f)
    val imageProxyFps: StateFlow<Float> = _imageProxyFps.asStateFlow()
    private val _previewViewFps = MutableStateFlow(0f)
    val previewViewFps: StateFlow<Float> = _previewViewFps.asStateFlow()

    private val _inferenceFps = MutableStateFlow(0f)
    val inferenceFps: StateFlow<Float> = _inferenceFps.asStateFlow()


    init {

    }

    fun  updateImageProxyFPS(){
        imageProxyFpsCounter.tick("Analyzer FPS")
        _imageProxyFps.value = imageProxyFpsCounter.fps.toFloat()
    }
    fun parseNetworkCallResponse(json: String): DetectionResponse {
        val a = gson.fromJson(json, DetectionResponse::class.java)
        Log.d(NetworkDebugTag, "parseResponse: ${a.detections.size}")
        return a
    }


    fun uploadImageV2(jpegBytes: ByteArray) {

        if (_isProcessing.value) return

        _isProcessing.value = true
        //Log.d(NetworkDebugTag, "API called")
        val now = System.currentTimeMillis()

        val requestBody = jpegBytes.toRequestBody("image/jpeg".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL/infer/json")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e(NetworkDebugTag, "FAILED: ${e.message}")
                _isProcessing.value = false
            }

            override fun onResponse(call: Call, response: Response) {

                val statusCode = response.code
                val bodyString = response.body.string()
                Log.d(NetworkDebugTag, "RESPONSE: $bodyString")
                Log.d(NetworkDebugTag, "STATUS: $statusCode")

                if (response.isSuccessful) { // same as (code in 200..299)
                    _detectedObjects.value =
                        parseNetworkCallResponse(bodyString).detections
                    updateInferenceFPS()
                } else {
                    Log.e(NetworkDebugTag, "Error response: $statusCode")
                    // optionally handle error body here
                }
                _isProcessing.value = false
                Log.d(PerformanceDebugTag, "viewModel: uploadImageV2: total execution time: ${System.currentTimeMillis()-now}")
                /*viewModelScope.launch {
                    delay(200) // 200ms delay
                    _isProcessing.value = false
                }*/
            }
        })
    }

    fun testUploadImage(jpegBytes: ByteArray) {
        Log.d(NetworkDebugTag, "called")

        if (_isProcessing.value) return
        _isProcessing.value = true



        val requestBody = jpegBytes.toRequestBody("image/jpeg".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL/test_res")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e(NetworkDebugTag, "FAILED: ${e.message}")
                _isProcessing.value = false
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body.string()
                Log.d(NetworkDebugTag, "RESPONSE: $json")
                _isProcessing.value = false
            }
        })
    }


    var timeForSaveImage = System.currentTimeMillis()
    // /storage/emulated/0/Android/data/com.example.cameraobjectanalyzer/files/debug_xxx.jpg
    fun saveImageProxy(context: Context, jpegBytes:  ByteArray, rotationDegree: Float) {


        if (System.currentTimeMillis() - timeForSaveImage < 1000) return

        try {
            val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)

            //metadata about how to transform geometry. stores a mathematical transformation description
            val matrix = Matrix()
            //“When you draw this image, rotate every pixel by X degrees around a pivot.”
            matrix.postRotate(rotationDegree)

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.width, bitmap.height,
                matrix, true
            )

            saveImageProxy(context,rotatedBitmap)

        } finally {

        }
    }



     fun saveImageProxy(context: Context, bitmap: Bitmap ) {

         if (System.currentTimeMillis() - timeForSaveImage < 1000) return

        try {
            val file = File(
                context.getExternalFilesDir(null),
                "debug_${System.currentTimeMillis()}.jpg"
            )

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            Log.d("DEBUG_IMAGE", "Saved: ${file.absolutePath}")

        } finally {

        }
    }




    fun viewModelFPSRateLimit(){
        if (_isProcessing.value) return

        _isProcessing.value = true
        Log.d(PerformanceDebugTag, "viewModelFPSRateLimit")
        updateInferenceFPS()

        viewModelScope.launch {
            delay(1000) // performance delay
            _isProcessing.value = false
        }
    }

    fun debugInfo(imgProxy:ImageProxy,previewView: PreviewView){
        if (_isProcessing.value) return


        Log.d(ImageDebugTag, "viewModel:debugInfo: imageProxy-> rotation:${imgProxy.imageInfo.rotationDegrees}")
        Log.d(ImageDebugTag, "viewModel:debugInfo: imageProxy-> width:${imgProxy.width} height: ${imgProxy.height}")
        Log.d(ImageDebugTag, "viewModel:debugInfo: imageProxy-> format:${imgProxy.format}")

        Log.d(ImageDebugTag, "viewModel:debugInfo: previewView-> width:${previewView.width} height: ${previewView.height}")
        Log.d(ImageDebugTag, "viewModel:debugInfo: previewView-> rotation:${previewView.display.rotation}")
        //Log.d(ImageDebugTag, "Canvas: Canvas: width:${size.width} height: ${size.height}")

    }



    fun updateInferenceFPS() {
        inferenceFpsCounter.tick("Inference FPS")
        _inferenceFps.value = inferenceFpsCounter.fps.toFloat()
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun updateDisplayFps(frameCount: Int) {
        _previewViewFps.value = frameCount.toFloat()
    }
}