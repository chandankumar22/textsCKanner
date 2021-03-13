package com.ck.textsckanner.ui.components.takepicture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.ck.textsckanner.R
import com.ck.textsckanner.utils.UiUtils.toast
import com.google.android.material.button.MaterialButton

class CaptureImageFragment : Fragment() {


    private var camera: androidx.camera.core.Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_capture_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            startCamera(view)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                0
            )
        }
        view.findViewById<MaterialButton>(R.id.capture_image).setOnClickListener {
            takePhoto()
        }
    }

    private fun takePhoto() {
        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {

                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    val errorType = exception.getImageCaptureError()
                    requireActivity().toast(exception.message!!)
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            startCamera(requireView())
        } else {
            Toast.makeText(
                context,
                "Dude camera is require for capturing images",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun startCamera(view: View) {
        val cameraFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraFuture.addListener({
            val cameraProvider = cameraFuture.get()
            preview = Preview.Builder().build()
            with(view.findViewById<PreviewView>(R.id.camer_preview)) {
                preview?.setSurfaceProvider(createSurfaceProvider(camera?.cameraInfo))
            }
            imageCapture = ImageCapture.Builder().build()
            val cs =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cs, preview, imageCapture)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

}