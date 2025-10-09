package com.example.edgedetectionviewer

import android.graphics.Bitmap
import android.util.Log
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

object EdgeDetector {

    private const val TAG = "EdgeDetector"

    // Processing modes
    enum class Mode {
        NONE,           // Original
        GRAYSCALE,      // Black & white
        EDGE_DETECTION  // Canny edges
    }

    fun processFrame(inputBitmap: Bitmap, mode: Mode): Bitmap? {
        return try {
            when (mode) {
                Mode.NONE -> inputBitmap
                Mode.GRAYSCALE -> applyGrayscale(inputBitmap)
                Mode.EDGE_DETECTION -> applyEdgeDetection(inputBitmap)
            }
        } catch (e: Exception) {
            Log.e(TAG, " Processing error", e)
            null
        }
    }

    private fun applyGrayscale(inputBitmap: Bitmap): Bitmap? {
        return try {
            val inputMat = inputBitmap.toMat()
            val grayMat = Mat()

            Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY)
            Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_GRAY2RGBA)

            val outputBitmap = grayMat.toBitmap()

            inputMat.release()
            grayMat.release()

            outputBitmap
        } catch (e: Exception) {
            Log.e(TAG, " Grayscale error", e)
            null
        }
    }

    private fun applyEdgeDetection(inputBitmap: Bitmap): Bitmap? {
        return try {
            val inputMat = inputBitmap.toMat()
            val grayMat = Mat()
            val blurMat = Mat()
            val edgesMat = Mat()

            // Convert to grayscale
            Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY)

            // Apply Gaussian blur to reduce noise
            Imgproc.GaussianBlur(grayMat, blurMat, Size(5.0, 5.0), 1.5)

            // Apply Canny edge detection
            Imgproc.Canny(blurMat, edgesMat, 50.0, 150.0)

            // Convert to RGBA for display
            Imgproc.cvtColor(edgesMat, edgesMat, Imgproc.COLOR_GRAY2RGBA)

            val outputBitmap = edgesMat.toBitmap()

            // Release all Mats
            inputMat.release()
            grayMat.release()
            blurMat.release()
            edgesMat.release()

            Log.d(TAG, " Edge detection successful")
            outputBitmap

        } catch (e: Exception) {
            Log.e(TAG, " Edge detection error", e)
            null
        }
    }
}
