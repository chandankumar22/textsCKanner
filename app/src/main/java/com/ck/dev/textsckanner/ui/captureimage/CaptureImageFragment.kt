package com.ck.dev.textsckanner.ui.captureimage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.ck.dev.textsckanner.R
import com.ck.dev.textsckanner.ui.detectedtext.DetectedTextActivity
import com.ck.dev.textsckanner.utils.Utility.bitmap
import com.ck.dev.textsckanner.utils.Utility.detectedText
import com.ck.dev.textsckanner.utils.Utility.getImageUri
import com.ck.dev.textsckanner.utils.Utility.saveImageToInternal
import com.ck.dev.textsckanner.utils.showToast
import com.ck.dev.textsckanner.utils.toBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_capture_image.*
import timber.log.Timber
import java.io.File


class CaptureImageFragment : Fragment(R.layout.fragment_capture_image) {

    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File

    companion object {
        @JvmStatic
        fun newInstance() = CaptureImageFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCamera()
        outputDirectory = getOutputDirectory()
        setListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        capture_image.setOnClickListener {
            takePicture()
        }
        capture_fragment_toolbar.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_BOTTOM = 3
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.x >= capture_fragment_toolbar.right - capture_fragment_toolbar.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                    - capture_fragment_toolbar.paddingRight
                ) {
                    extractTextFromImage()
                    return@OnTouchListener true
                } else if (event.x >= capture_fragment_toolbar.left - capture_fragment_toolbar.compoundDrawables[DRAWABLE_LEFT].bounds.width()
                    - capture_fragment_toolbar.paddingLeft
                ) {
                    findNavController().navigate(R.id.action_captureImageFragment_to_homeFragment)
                    return@OnTouchListener true
                }
            }
            true
        })
        retake_btn.setOnClickListener {
            layout_2.visibility = View.GONE
            layout_1.visibility = View.VISIBLE
        }
    }

    private fun extractTextFromImage() {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient()
        recognizer.process(image)
            .addOnSuccessListener { texts ->
                /*
                  val a = texts.text
                  val b = texts.textBlocks
                  val c = texts.textBlocks.size
                  val d = b[0]
                  val e = d.lines
                  val f = e.size
                  val g = e[0]
                  val h = g.elements
                  val i = h.size
                  val j = h[0]
                  val k = j.text
                  val l = j.recognizedLanguage
                  val m = j.cornerPoints*/
                Timber.i(texts.text)
                detectedText = texts.text
                //savePhotoToStorage(requireContext(),bitmap)
                saveImageToInternal(bitmap)
                startActivity(Intent(requireContext(), DetectedTextActivity::class.java))
                /*mTextButton.setEnabled(true)
                processTextRecognitionResult(texts)*/
            }
            .addOnFailureListener { e -> // Task failed with an exception
                //  mTextButton.setEnabled(true)
                e.printStackTrace()
            }
    }

    private fun startCamera() {
        val cameraFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraFuture.addListener(Runnable {
            val cameraProvider = cameraFuture.get()
            preview = Preview.Builder().build()
            camera_preview.apply {
                preview?.setSurfaceProvider(createSurfaceProvider(camera?.cameraInfo))
            }
            imageCapture = ImageCapture.Builder().build()
            val cs =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cs, preview, imageCapture)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePicture() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                @SuppressLint("UnsafeExperimentalUsageError")
                override fun onCaptureSuccess(image: ImageProxy) {
                    layout_1.visibility = View.GONE
                    layout_2.visibility = View.VISIBLE
                    bitmap = image.image?.toBitmap()!!
                    val requestOptions = RequestOptions().override(100)
                        .downsample(DownsampleStrategy.CENTER_INSIDE)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)

                    Glide.with(requireContext()).asBitmap().load(bitmap)
                        .apply(requestOptions)
                        .into(object : SimpleTarget<Bitmap?>() {
                            override fun onResourceReady(
                                @NonNull resource: Bitmap,
                                @Nullable transition: Transition<in Bitmap?>?
                            ) {
                                bitmap = resource
                                captured_image_view.setImageBitmap(bitmap)
                            }
                        })
                 /*
                    Glide.with(requireActivity())
                        .asBitmap()
                        .load(bitmap)
                        .into(captured_image_view)*/
                    crop_rotate_btn.setOnClickListener {
                        val intent = CropImage.activity(getImageUri(requireContext(), bitmap))
                            .getIntent(context!!)
                        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
                    }

                    layout_2.apply {
                        visibility = View.VISIBLE
                        requireView().findViewById<CropImageView>(R.id.cropImageView)
                            .apply {
                                Glide.with(requireActivity())
                                    .asBitmap()
                                    .load(image.image?.toBitmap())
                                    .into(captured_image_view)

                            }
                    }
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Timber.e(exception, exception.message!!)
                    requireActivity().showToast(exception.message!!)
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri: Uri = result.uri
                bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().contentResolver,
                    resultUri
                )
                Timber.d("resultUri ->${java.lang.String.valueOf(resultUri)}")

                Glide.with(requireContext()).asBitmap().load(resultUri)
                    .apply(RequestOptions().override(bitmap.height, bitmap.width))
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .into(object : SimpleTarget<Bitmap?>() {
                        override fun onResourceReady(
                            @NonNull resource: Bitmap,
                            @Nullable transition: Transition<in Bitmap?>?
                        ) {
                            bitmap = resource
                            captured_image_view.setImageBitmap(bitmap)
                        }
                    })
               /* Glide.with(requireActivity())
                    .load(resultUri)
                    .into(captured_image_view)*/
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Timber.e(error, error.toString())
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = requireContext().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireContext().filesDir
    }
}