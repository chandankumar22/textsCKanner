package com.ck.dev.textsckanner.ui.home

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.ck.dev.textsckanner.R
import com.ck.dev.textsckanner.data.AppDatabaseHelperImpl
import com.ck.dev.textsckanner.data.DatabaseFactory
import com.ck.dev.textsckanner.ui.Application
import com.ck.dev.textsckanner.ui.ScannedDocument
import com.ck.dev.textsckanner.utils.*
import com.ck.dev.textsckanner.utils.Constants.SORT_KEY
import com.ck.dev.textsckanner.utils.Utility.bitmap
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home), AdapterCallback {

    private val viewModel: HomeViewModel by viewModels() {
        ViewModelFactory(
            AppDatabaseHelperImpl(
                DatabaseFactory.getInstance(requireContext())
            )
        )
    }
    private lateinit var docAdapter: ScannedDocumentAdapter
    private val docsListForAdapter = arrayListOf<ScannedDocument>()

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "user-pref"
    private lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated called")
        super.onViewCreated(view, savedInstanceState)
        sharedPref = requireContext().getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        camera_btn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                ) == PermissionChecker.PERMISSION_GRANTED
            ) {
                goToCaptureScreen()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA),
                    0
                )
            }

        }
        gallery_btn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.CAMERA
                ) == PermissionChecker.PERMISSION_GRANTED
            ) {
                openGalleryAndSaveUserImage()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA),
                    0
                )
            }
        }
        viewModel.getAllScannedDocLive().observe(viewLifecycleOwner, Observer { docs ->
            if (docs.isEmpty()) {
                Application.docFilenames.clear()
                scanned_doc_rv.visibility = View.GONE
                sort_by_tv.visibility = View.GONE
                sort_by_rg.visibility = View.GONE
                sort_by_rg.visibility = View.GONE
                search_ic.visibility = View.GONE
                app_name_tv.visibility = View.VISIBLE
                app_logo.visibility = View.VISIBLE
                welcome_content.visibility = View.VISIBLE
            } else {
                Application.docFilenames.clear()
                docsListForAdapter.clear()
                docs.forEach {
                    Application.docFilenames.add(it.documentName)
                    docsListForAdapter.add(
                        ScannedDocument(
                            documentName = it.documentName,
                            documentTitle = it.documentTitle,
                            imgPath = it.imgPath,
                            docPath = it.docPath,
                            creationDate = it.creationDate,
                            extension = it.extension
                        )
                    )
                }
                scanned_doc_rv.layoutManager = GridLayoutManager(requireContext(), 2)
                scanned_doc_rv.visibility = View.VISIBLE
                app_name_tv.visibility = View.GONE
                app_logo.visibility = View.GONE
                welcome_content.visibility = View.GONE
                docAdapter = ScannedDocumentAdapter(
                    docsListForAdapter,
                    docsListForAdapter,
                    this@HomeFragment
                )
                scanned_doc_rv.adapter = docAdapter
                with(sharedPref.getInt(SORT_KEY, 0)) {
                    if (this == 0) {
                        sharedPref.edit().putInt(SORT_KEY, 1).apply()
                        name_rb.isChecked = true
                    } else {
                        when (this) {
                            1 -> {
                                name_rb.isChecked = true
                                //  sortByName()
                            }
                            2 -> {
                                date_rb.isChecked = true
                                // sortByDate()
                            }
                            3 -> {
                                size_rb.isChecked = true
                                //   sortBySize()
                            }
                        }
                    }
                }
            }
        })
        setListeners()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.i("onRequestPermissionsResult called")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_NAME, "permission_granted")
            }
            (requireActivity().application as Application).firebaseAnalytics.logEvent("permission_granted",bundle)
            goToCaptureScreen()
        } else {
            requireContext().showToast("Please provide permissions to access device camera")
        }
    }


    private fun sortByName() {
        Timber.i("sortByName called on size : ${docsListForAdapter.size}")
        if (docsListForAdapter.isNotEmpty()) {
            docsListForAdapter.sortBy {
                it.documentName
            }
            docAdapter.notifyDataSetChanged()
            sharedPref.edit().putInt(SORT_KEY, 1).apply()
            hideSelectedToolbarItems()
            docAdapter.removeAllSelected()
        }
    }

    private fun sortBySize() {
        Timber.i("sortBySize called on size : ${docsListForAdapter.size}")
        if (docsListForAdapter.isNotEmpty()) {
            docsListForAdapter.sortBy {
                it.size
            }
            docAdapter.notifyDataSetChanged()
            sharedPref.edit().putInt(SORT_KEY, 3).apply()
            hideSelectedToolbarItems()
            docAdapter.removeAllSelected()
        }
    }

    private fun sortByDate() {
        Timber.i("sortByDate called on size : ${docsListForAdapter.size}")
        if (docsListForAdapter.isNotEmpty()) {
            docsListForAdapter.sortBy { scanned ->
                scanned.creationDate?.let {
                    val dateTime = it.split("at")
                    val date = dateTime[0].trim()
                    val time = dateTime[1].trim()
                    val format =
                        SimpleDateFormat("dd MMMM yyyy hh:mm:ss", Locale.getDefault())
                    format.parse("$date $time")

                }
            }
            docAdapter.notifyDataSetChanged()
            sharedPref.edit().putInt(SORT_KEY, 2).apply()
            hideSelectedToolbarItems()
            docAdapter.removeAllSelected()
        }
    }

    private fun setListeners() {
        Timber.i("setListeners called")
        back_btn.setOnClickListener {
            docAdapter.removeAllSelected()
            hideSelectedToolbarItems()
            docAdapter.reset()
        }
        share_ic.setOnClickListener {
            val listOfSelectedDoc = docAdapter.getSelectedDoc()
            val listOfDocPaths = arrayListOf<String>()
            listOfSelectedDoc.forEach {
                it.docPath?.let { docPath ->
                    listOfDocPaths.add(docPath)
                }
                (requireActivity() as AppCompatActivity).apply {
                    try {
                        shareDocuments(
                            listOfDocPaths,
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        )
                    } catch (ex: Exception) {
                        showToast("Sharing failed. Please try again")
                    }
                }
            }
        }
        delete_ic.setOnClickListener {
            showDeleteDialog()
        }
        search_ic.setOnClickListener {
            if (search_bar.visibility == View.GONE) {
                showSearchableView()
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.ITEM_NAME, "document_search")
                }
                (requireActivity().application as Application).firebaseAnalytics.logEvent("document_search",bundle)
            }
        }
        search_bar.doAfterTextChanged {
            filterListAccordingToSearch(search_bar.text.toString().trim())
            /* else {
                 scanned_doc_rv.adapter = ScannedDocumentAdapter(docsListForAdapter, docsListForAdapter, this@HomeFragment)
             }*/
        }
        sort_by_rg.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.name_rb -> {
                    sortByName()
                }
                R.id.date_rb -> {
                    sortByDate()
                }
                R.id.size_rb -> {
                    sortBySize()
                }
            }
        }
    }

    private fun filterListAccordingToSearch(searchQuery: String) {
        Timber.i("filterListAccordingToSearch called")
        if (this::docAdapter.isInitialized) docAdapter.filter.filter(searchQuery)
    }

    override fun onResume() {
        Timber.i("onResume called")
        super.onResume()
        Application.isEdit = false
        Utility.detectedText = ""
    }

    private fun showDeleteDialog() {
        Timber.i("showDeleteDialog called")
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.layout_delete_dialog)
        val params: ViewGroup.LayoutParams = dialog.window!!.attributes
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        dialog.setCancelable(false)
        dialog.findViewById<MaterialTextView>(R.id.del_dialog_content).text =
            getString(R.string.confirm_delete_content, docAdapter.getSelectedDoc().size.toString())
        dialog.findViewById<MaterialButton>(R.id.del_dialog_neg_btn)
            .setOnClickListener { dialog.dismiss() }
        dialog.findViewById<MaterialButton>(R.id.del_dialog_pos_btn).apply {
            setOnClickListener {
                val listOfSelectedDoc = docAdapter.getSelectedDoc()
                val listOfDocuments = arrayListOf<com.ck.dev.textsckanner.data.ScannedDocument>()
                listOfSelectedDoc.forEach {
                    listOfDocuments.add(
                        com.ck.dev.textsckanner.data.ScannedDocument(
                            documentName = it.documentName,
                            documentTitle = it.documentTitle,
                            imgPath = it.imgPath,
                            extension = it.extension,
                            docPath = it.docPath,
                            creationDate = it.creationDate
                        )
                    )
                    (requireActivity() as AppCompatActivity).apply {
                        deleteImageInInternal(it.imgPath!!)
                        deleteImageInInternal(it.docPath!!)
                    }
                }
                viewModel.deleteDocument(listOfDocuments)
                docAdapter.notifyItemRangeRemoved(
                    docAdapter.getSelectedIndex()[docAdapter.getSelectedIndex().size - 1],
                    listOfDocuments.size
                )
                val bundle = Bundle().apply {
                    putString(FirebaseAnalytics.Param.ITEM_NAME, "document_delete")
                }
                (requireActivity().application as Application).firebaseAnalytics.logEvent("document_delete",bundle)
                dialog.dismiss()
                hideSelectedToolbarItems()
            }
        }
        dialog.show()
    }

    private fun goToCaptureScreen() {
        Timber.i("goToCaptureScreen called")
        Application.isGalleryImage = false
        findNavController().navigate(R.id.action_homeFragment_to_captureImageFragment)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }

    override fun onItemSelect(isSelected: Boolean, count: Int) {
        if (isSelected) {
            showSelectedToolbarItems(count.toString())
        } else {
            hideSelectedToolbarItems()
        }
    }

    private fun hideSelectedToolbarItems() {
        Timber.i("hideSelectedToolbarItems called")
        delete_ic.visibility = View.GONE
        share_ic.visibility = View.GONE
        back_btn.visibility = View.GONE
        selected_count.visibility = View.GONE
        search_bar.visibility = View.GONE
        search_ic.visibility = View.VISIBLE
        toolbar.text = getString(R.string.toolbar_home_title)
    }

    private fun showSelectedToolbarItems(count: String) {
        Timber.i("showSelectedToolbarItems called")
        delete_ic.visibility = View.VISIBLE
        share_ic.visibility = View.VISIBLE
        back_btn.visibility = View.VISIBLE
        search_bar.visibility = View.GONE
        search_bar.setText("")
        selected_count.visibility = View.VISIBLE
        selected_count.text = count
        search_ic.visibility = View.GONE
        toolbar.text = ""
    }

    private fun showSearchableView() {
        Timber.i("showSearchableView called")
        delete_ic.visibility = View.GONE
        share_ic.visibility = View.GONE
        search_ic.visibility = View.GONE
        back_btn.visibility = View.GONE
        selected_count.visibility = View.GONE
        toolbar.text = ""
        back_btn.visibility = View.VISIBLE
        search_bar.visibility = View.VISIBLE
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
                                            findNavController().navigate(R.id.action_homeFragment_to_captureImageFragment)
                                            /*  showProgressDialog()
                                              val byteArrayOutputStream = ByteArrayOutputStream()
                                              currentDocumentBitmap!!.compress(
                                                  Bitmap.CompressFormat.PNG,
                                                  100,
                                                  byteArrayOutputStream
                                              )
                                              val docByteArray = byteArrayOutputStream.toByteArray()
                                              currentDocListOfExt = ArrayList()
                                              currentDocListOfExt.add(documentUriExt)
                                              uploadDocument(
                                                  currentSelectedDoc!!,
                                                  docByteArray,
                                                  currentDocListOfExt
                                              )*/
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