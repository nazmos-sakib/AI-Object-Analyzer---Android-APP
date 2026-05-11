package com.example.cameraobjectanalyzer

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cameraobjectanalyzer.presentation.CameraScreen
import com.example.cameraobjectanalyzer.ui.theme.CameraObjectAnalyzerTheme
import com.example.cameraobjectanalyzer.presentation.viewmodel.CameraViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import okhttp3.OkHttpClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) //keep the screen always on
        enableEdgeToEdge()
        setContent {
            CameraObjectAnalyzerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainCompose(Modifier.padding(innerPadding))
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}



@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainCompose(
    modifier: Modifier
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)


     val viewModel: CameraViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                CameraViewModel(

                )
            }
        }
    )

    when {
        cameraPermissionState.status.isGranted -> {
            CameraScreen(viewModel = viewModel)
        }
        cameraPermissionState.status.shouldShowRationale -> {
            // Show rationale for camera permission
            Text("Camera permission is needed for object detection")
        }
        else -> {
            // Request permission
            cameraPermissionState.launchPermissionRequest()
        }
    }
}