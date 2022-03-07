package com.example.opencvexample

import android.graphics.Bitmap
import android.view.Surface
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class CustomImageAnalyzer(private val rotation: Int, private val onResultCallback: ((Bitmap) -> Unit)): ImageAnalysis.Analyzer {
    private var matPrevious: Mat? = null

    override fun analyze(image: ImageProxy) {
        val matOrg = getMatFromImage(image)
        val mat = matRotation(matOrg, rotation)

        val matOutput = Mat(mat.rows(), mat.cols(), mat.type())
        if (matPrevious == null) {
            matPrevious = mat
        }
        Core.absdiff(mat, matPrevious, matOutput)
        matPrevious = mat

        val bitmap = Bitmap.createBitmap(matOutput.cols(), matOutput.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(matOutput, bitmap)
        onResultCallback.invoke(bitmap)

        image.close()
    }

    private fun getMatFromImage(image: ImageProxy): Mat {
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
        val yuv = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
        yuv.put(0, 0, nv21)
        val mat = Mat()
        Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2RGB_NV21, 3)
        return mat
    }

    private fun matRotation(matOrg: Mat, rotation: Int): Mat {
        when (rotation) {
            Surface.ROTATION_0 -> {
                val mat = Mat(matOrg.cols(), matOrg.rows(), matOrg.type())
                Core.transpose(matOrg, mat)
                Core.flip(mat, mat, 1)
                return mat
            }
            Surface.ROTATION_90 -> {
                val mat = matOrg
                return mat
            }
            Surface.ROTATION_270 -> {
                val mat = matOrg
                Core.flip(mat, mat, -1)
                return mat
            }
            else -> return matOrg
        }
    }
}