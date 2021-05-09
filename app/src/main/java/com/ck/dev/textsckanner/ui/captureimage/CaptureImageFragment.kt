package com.ck.dev.textsckanner.ui.captureimage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
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
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ck.dev.textsckanner.R
import com.ck.dev.textsckanner.ui.Application
import com.ck.dev.textsckanner.ui.detectedtext.DetectedTextActivity
import com.ck.dev.textsckanner.utils.Utility.bitmap
import com.ck.dev.textsckanner.utils.Utility.detectedText
import com.ck.dev.textsckanner.utils.Utility.getImageUri
import com.ck.dev.textsckanner.utils.showToast
import com.ck.dev.textsckanner.utils.toBitmap
import com.google.firebase.analytics.FirebaseAnalytics
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
        outputDirectory = getOutputDirectory()
        setListeners()
        if (Application.isGalleryImage) {
            handleImageFromGalleryScenario()
        } else {
            startCamera()
        }
    }

    private fun handleImageFromGalleryScenario() {
        layout_2.visibility = View.VISIBLE
        layout_1.visibility = View.GONE
        //retake_btn.visibility = View.GONE
        Glide.with(requireContext()).load(bitmap).into(captured_image_view)
        retake_btn.text = "Re-take"
        retake_btn.setOnClickListener {
            openGalleryAndSaveUserImage()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        capture_image.setOnClickListener {
            takePicture()
        }
        scan_btn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            extractTextFromImage()
        }
        capture_back_btn.setOnClickListener {
            findNavController().navigate(R.id.action_captureImageFragment_to_homeFragment)
        }
        retake_btn.setOnClickListener {
            layout_2.visibility = View.GONE
            layout_1.visibility = View.VISIBLE
        }
        crop_rotate_btn.setOnClickListener {
            val intent = CropImage.activity(getImageUri(requireContext(), bitmap))
                .getIntent(requireContext())
            startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
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
                progressBar.visibility = View.GONE
                //savePhotoToStorage(requireContext(),bitmap)
                logFirebaseEvent(texts.text)
                if (detectedText.isEmpty()){
                    requireContext().showToast("No text found in image.")
                }
                else {
                    startActivity(Intent(requireContext(), DetectedTextActivity::class.java))
                }
                /*mTextButton.setEnabled(true)
                processTextRecognitionResult(texts)*/

            }
            .addOnFailureListener { e -> // Task failed with an exception
                //  mTextButton.setEnabled(true)
                requireContext().showToast("Error when scanning text. Please try again.")
                e.printStackTrace()
                progressBar.visibility = View.GONE
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.ITEM_NAME, "text_scan_failed")
                }
                (requireActivity().application as Application).firebaseAnalytics.logEvent("text_scan_failed",bundle)
            }
    }

    private fun logFirebaseEvent(str: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, "text_scan_success")
            putString("text_scanned_character", str.length.toString())
        }
        (requireActivity().application as Application).firebaseAnalytics.logEvent("text_scan_success",bundle)

        if(Application.isGalleryImage){
            val galleryBundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_NAME, "gallery_image")
            }
            (requireActivity().application as Application).firebaseAnalytics.logEvent("gallery_image",galleryBundle)
        }else{
            val cameraImg = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_NAME, "camera_image")
            }
            (requireActivity().application as Application).firebaseAnalytics.logEvent("camera_image",cameraImg)
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
                        .into(object : CustomTarget<Bitmap?>() {
                            override fun onResourceReady(
                                @NonNull resource: Bitmap,
                                @Nullable transition: Transition<in Bitmap?>?
                            ) {
                                bitmap = resource
                                captured_image_view.setImageBitmap(bitmap)
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {

                            }
                        })
                    /*
                       Glide.with(requireActivity())
                           .asBitmap()
                           .load(bitmap)
                           .into(captured_image_view)*/
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
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            @NonNull resource: Bitmap,
                            @Nullable transition: Transition<in Bitmap?>?
                        ) {
                            bitmap = resource
                            captured_image_view.setImageBitmap(bitmap)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

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

    private fun openGalleryAndSaveUserImage() {
        Timber.i("openGalleryAndSaveUserImage called")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val mimetypes = arrayOf("image/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        launchSomeActivity.launch(intent)
    }

    private var launchSomeActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data

                if (data != null && data.data != null) {
                    try {
                        val documentUri: Uri = data.data!!
                        var documentUriExt =
                            MimeTypeMap.getFileExtensionFromUrl(documentUri.toString())
                        if (documentUri.scheme!! == ContentResolver.SCHEME_CONTENT) {
                            val mime = MimeTypeMap.getSingleton()
                            val ext = mime.getExtensionFromMimeType(
                                requireActivity().contentResolver.getType(
                                    documentUri
                                )
                            )
                            if (ext != null) documentUriExt = ext

                        } else {
                            documentUriExt =
                                MimeTypeMap.getFileExtensionFromUrl(
                                    Uri.fromFile(File(documentUri.path!!)).toString()
                                )
                        }
                        if (documentUriExt.isNotEmpty()) {
                            val type =
                                MimeTypeMap.getSingleton().getMimeTypeFromExtension(documentUriExt)
                            if (type == "image/jpeg" || type == "image/jpg" || type == "image/png") {
                                Glide.with(requireContext()).asBitmap().load(documentUri)
                                    .into(object : CustomTarget<Bitmap?>() {
                                        override fun onResourceReady(
                                            @NonNull resource: Bitmap,
                                            @Nullable transition: Transition<in Bitmap?>?
                                        ) {
                                            bitmap = resource
                                            Application.isGalleryImage = true
                                            handleImageFromGalleryScenario()
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {

                                        }
                                    })
                            } else {
                                requireActivity().showToast("Invalid file format")
                            }
                        } else {
                            requireActivity().showToast("Invalid file format")
                        }
                    } catch (ex: Exception) {
                        Timber.e(ex)
                        requireActivity().showToast("File is corrupted")
                    }
                }
            }
        }
}