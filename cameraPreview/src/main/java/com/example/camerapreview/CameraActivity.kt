package com.example.camerapreview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.camerapreview.databinding.ActivityCameraPreviewBinding
import java.io.File

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraPreviewBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo

    // UseCase
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null

    // Camera State
    private var isFlashOn = false
    private var isFrontCamera = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_preview)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        startCamera()
        setListeners()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            try {
                bindCameraUseCases()
            } catch (e: Exception) {
                Log.e("CameraX", "카메라 시작 실패", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        preview = Preview.Builder().build().apply {
            surfaceProvider = binding.cameraPreview.surfaceProvider
        }
        imageCapture = ImageCapture.Builder()
            .setTargetRotation(binding.cameraPreview.display.rotation)
            .build()

        val cameraSelector =
            if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA

        cameraProvider.unbindAll()

        val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        cameraControl = camera.cameraControl
        cameraInfo = camera.cameraInfo
    }

    private fun setListeners() {
        binding.btnCapture.setOnClickListener { takeCapture() }
        binding.btnFlash.setOnClickListener { toggleFlashlight() }
        binding.btnTransition.setOnClickListener { toggleCamera() }
    }

    private fun takeCapture() {
        imageCapture?.let { capture ->
            val imageFile = File(getExternalFilesDir(null), "captured_image.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

            capture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e("CameraX", "사진 촬영 실패: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = Uri.fromFile(imageFile)
                        val resultIntent = Intent().apply {
                            putExtra("imageUri", savedUri.toString())
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                }
            )
        }
    }

    private fun toggleFlashlight() {
        cameraControl.enableTorch(!isFlashOn)
        isFlashOn = !isFlashOn
    }

    private fun toggleCamera() {
        isFrontCamera = !isFrontCamera
        bindCameraUseCases()
    }
}