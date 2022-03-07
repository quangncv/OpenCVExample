package com.example.opencvexample

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.OpenCVLoader
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV installed.")
        } else {
            Log.d(TAG, "OpenCV is not installed.")
        }

        checkPermissions()
    }

    private val permissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        }
    }

    private fun checkPermissions() {
        permissionResult.launch(
            Manifest.permission.CAMERA
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder().build()
                imageAnalysis = ImageAnalysis.Builder().build()
                imageAnalysis?.setAnalyzer(cameraExecutor, CustomImageAnalyzer(cameraView.display.rotation) { bm ->
                    runOnUiThread {
                        imgPreview.setImageBitmap(bm)
                    }
                })
                val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageAnalysis)
                preview?.setSurfaceProvider(cameraView.surfaceProvider)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}