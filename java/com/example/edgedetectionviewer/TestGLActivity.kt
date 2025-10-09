package com.example.edgedetectionviewer

import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class TestGLActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestGLScreen()
        }
    }
}

@Composable
fun TestGLScreen() {
    val glRenderer = remember { SimpleGLRenderer() }

    // Create test bitmap
    LaunchedEffect(Unit) {
        val testBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        testBitmap.eraseColor(Color.GREEN)
        glRenderer.currentBitmap = testBitmap
    }

    AndroidView(
        factory = { context ->
            GLSurfaceView(context).apply {
                setEGLContextClientVersion(2)
                setRenderer(glRenderer)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
