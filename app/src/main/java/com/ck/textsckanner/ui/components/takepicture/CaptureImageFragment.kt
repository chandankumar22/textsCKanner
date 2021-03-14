package com.ck.textsckanner.ui.components.takepicture

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.ck.textsckanner.R
import com.ck.textsckanner.ui.components.savetext.DetectedTextActivity
import com.ck.textsckanner.utils.UiUtils.inspectFromBitmap
import com.ck.textsckanner.utils.UiUtils.toBitmap
import com.ck.textsckanner.utils.UiUtils.toast
import com.google.android.material.button.MaterialButton
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream


class CaptureImageFragment : Fragment() {


    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var layout1:RelativeLayout
    private lateinit var layout2:ConstraintLayout
    private lateinit var scanBtn:MaterialButton
    private lateinit var retakeBtn:MaterialButton
    private lateinit var cropBtn:MaterialButton
    private lateinit var imageView:AppCompatImageView
    private lateinit var bitmap:Bitmap
    private var croppedBitmap:Bitmap?=null

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
        layout1 =  view.findViewById(R.id.layout_1)
        layout2 =  view.findViewById(R.id.layout_2)
        cropBtn =  view.findViewById(R.id.crop_btn)
        scanBtn =  view.findViewById(R.id.scan_btn)
        retakeBtn =  view.findViewById(R.id.retake_btn)
        imageView =  view.findViewById(R.id.captured_image_view)
        retakeBtn.setOnClickListener {
            layout2.visibility = View.GONE
            layout1.visibility = View.VISIBLE
        }
        scanBtn.setOnClickListener {
            val text = requireContext().inspectFromBitmap(bitmap)
            val intent = Intent(requireContext(), DetectedTextActivity::class.java)
            intent.putExtra("text", text)
            startActivity(intent)
        }
    }

    fun getImageUri(inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Images.Media.insertImage(
            requireContext().getContentResolver(),
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    private fun takePhoto() {
        imageCapture?.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                @SuppressLint("UnsafeExperimentalUsageError")
                override fun onCaptureSuccess(image: ImageProxy) {
                    layout1.visibility = View.GONE
                    layout2.visibility = View.VISIBLE
                    bitmap = image.image?.toBitmap()!!
                    Glide.with(requireActivity())
                        .asBitmap()
                        .load(bitmap)
                        .into(imageView)
                    cropBtn.setOnClickListener {
                        val intent = CropImage.activity(getImageUri(bitmap))
                            .getIntent(context!!)
                        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
                    }

                    requireView().findViewById<ConstraintLayout>(R.id.layout_2).apply {
                        visibility = View.VISIBLE
                        requireView().findViewById<CropImageView>(R.id.cropImageView)
                            .apply {
                                /*Glide.with(requireActivity())
                                            .asBitmap()
                                            .load(image.image?.toBitmap())
                                            .into(this)*/

                            }
                    }
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    val errorType = exception.getImageCaptureError()
                    requireActivity().toast(exception.message!!)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri: Uri = result.uri
                bitmap = Images.Media.getBitmap(requireContext().getContentResolver(), resultUri)
                Log.e("resultUri ->", java.lang.String.valueOf(resultUri))
                Glide.with(requireActivity())
                    .load(resultUri)
                    .into(imageView)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.e("error ->", error.toString())
            }
        }
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