package com.example.camerapreviewex

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.camerapreview.CameraActivity
import com.example.camerapreviewex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val cameraPreviewLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.getStringExtra("imageUri")

                imageUri?.let {
                    binding.ivResult.setImageURI(null)
                    binding.ivResult.setImageURI(Uri.parse(it))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.btnCameraPreview.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            cameraPreviewLauncher.launch(intent)
        }

        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(this, permissions, 10)
    }
}