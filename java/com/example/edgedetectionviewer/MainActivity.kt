package com.example.edgedetectionviewer

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.edgedetectionviewer.ui.theme.EdgeDetectionViewerTheme
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    companion object {
        init {
            try {
                if (!OpenCVLoader.initDebug()) {
                    Log.e("MainActivity", " OpenCV init FAILED")
                } else {
                    Log.d("MainActivity", " OpenCV ${Core.VERSION}")
                }
                System.loadLibrary("native-lib")
                Log.d("MainActivity", " Native lib loaded")
            } catch (e: Exception) {
                Log.e("MainActivity", " Library load error", e)
            }
        }
        private const val TAG = "MainActivity"
    }

    external fun stringFromJNI(): String

    private lateinit var imageAnalysisExecutor: ExecutorService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "✅ Camera permission granted")
        } else {
            Toast.makeText(this, "❌ Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageAnalysisExecutor = Executors.newSingleThreadExecutor()

        requestCameraPermission()

        setContent {
            EdgeDetectionViewerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraPreviewScreen(imageAnalysisExecutor)
                }
            }
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Camera permission already granted")
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageAnalysisExecutor.shutdown()
    }
}

@Composable
fun CameraPreviewScreen(imageAnalysisExecutor: ExecutorService) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraWithGLToggle(imageAnalysisExecutor)
        } else {
            Text(
                text = "Camera permission required",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun CameraWithGLToggle(imageAnalysisExecutor: ExecutorService) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var fps by remember { mutableStateOf(0f) }
    var frameCount by remember { mutableIntStateOf(0) }
    var lastTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var processedCount by remember { mutableIntStateOf(0) }
    var currentMode by remember { mutableStateOf(EdgeDetector.Mode.EDGE_DETECTION) }

    //  FIXED: Use stable PreviewView only (No OpenGL toggle)
    Box(modifier = Modifier.fillMaxSize()) {
        // Normal Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(this.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also { analysis ->
                                    analysis.setAnalyzer(imageAnalysisExecutor) { imageProxy ->
                                        try {
                                            frameCount++
                                            val currentTime = System.currentTimeMillis()
                                            val elapsed = currentTime - lastTime

                                            if (elapsed >= 1000) {
                                                fps = frameCount * 1000f / elapsed
                                                frameCount = 0
                                                lastTime = currentTime
                                            }

                                            // Process frames (background)
                                            if (processedCount % 5 == 0) {
                                                val bitmap = imageProxy.toBitmap()
                                                if (bitmap != null) {
                                                    val processed = EdgeDetector.processFrame(bitmap, currentMode)
                                                    if (processed != null) {
                                                        Log.d("Camera", "✅ Processed frame")
                                                    }
                                                    bitmap.recycle()
                                                    processed?.recycle()
                                                }
                                            }
                                            processedCount++

                                        } catch (e: Exception) {
                                            Log.e("Camera", "Error", e)
                                        } finally {
                                            imageProxy.close()
                                        }
                                    }
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )

                            Log.d("Camera", " Camera bound")

                        } catch (e: Exception) {
                            Log.e("Camera", " Error", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Mode buttons - Top Center
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(35.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { currentMode = EdgeDetector.Mode.NONE },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentMode == EdgeDetector.Mode.NONE)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Original")
            }

            Button(
                onClick = { currentMode = EdgeDetector.Mode.GRAYSCALE },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentMode == EdgeDetector.Mode.GRAYSCALE)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Gray")
            }

            Button(
                onClick = { currentMode = EdgeDetector.Mode.EDGE_DETECTION },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentMode == EdgeDetector.Mode.EDGE_DETECTION)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Edges")
            }
        }

        // Mode indicator - Bottom Left
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Mode: ${when(currentMode) {
                    EdgeDetector.Mode.NONE -> "ORIGINAL"
                    EdgeDetector.Mode.GRAYSCALE -> "GRAYSCALE"
                    EdgeDetector.Mode.EDGE_DETECTION -> "EDGE DETECTION"
                }}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.White
            )
        }

        // FPS counter - Bottom Right
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "FPS: ${fps.toInt()}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = androidx.compose.ui.graphics.Color.Green
            )
        }

        //OpenCV Processing Status - Top Right
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(35.dp),
            color = androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.8f),
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Text(
                text = "●",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.titleLarge,
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}
