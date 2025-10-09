package com.example.edgedetectionviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.ByteArrayOutputStream

// Convert ImageProxy to Bitmap
@androidx.annotation.OptIn(ExperimentalGetImage::class)
fun ImageProxy.toBitmap(): Bitmap? {
    val image = this.image ?: return null

    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 50, out)

    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

// Convert Bitmap to OpenCV Mat
fun Bitmap.toMat(): Mat {
    val mat = Mat()
    Utils.bitmapToMat(this, mat)
    return mat
}

// Convert OpenCV Mat to Bitmap
fun Mat.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(cols(), rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(this, bitmap)
    return bitmap
}
