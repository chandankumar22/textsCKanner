package com.ck.dev.textsckanner.ui.detectedtext

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import com.ck.dev.textsckanner.R
import com.ck.dev.textsckanner.data.AppDatabaseHelperImpl
import com.ck.dev.textsckanner.data.DatabaseFactory
import com.ck.dev.textsckanner.data.ScannedDocument
import com.ck.dev.textsckanner.ui.Application
import com.ck.dev.textsckanner.utils.*
import com.ck.dev.textsckanner.utils.Constants.DOCUMENTS_DIR
import com.ck.dev.textsckanner.utils.Constants.saveError
import com.ck.dev.textsckanner.utils.Constants.saveSuccess
import com.ck.dev.textsckanner.utils.StorageUtils.isExternalStorageAvailable
import com.ck.dev.textsckanner.utils.StorageUtils.isExternalStorageReadOnly
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Event.SHARE
import kotlinx.android.synthetic.main.activity_detected_text.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class DetectedTextActivity : AppCompatActivity() {

    private var isSaved =false
    private var pubFileLoc: String = ""
    private lateinit var fileName: String
    private lateinit var tab1view: TabLayout.TabView
    private lateinit var tab2view: TabLayout.TabView
    private val viewModel: DetectedTextVm by viewModels {
        ViewModelDetectedTextFactory(
            AppDatabaseHelperImpl(
                DatabaseFactory.getInstance(applicationContext)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.i("onCreate called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detected_text)
        Application.viewModel = viewModel
        view_pager.adapter = DetectedTextAdapter(this)
        val tabLayoutStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.detected_text_tab_1)
                    tab1view = tab.view
                }
                1 -> {
                    tab.text = getString(R.string.detected_text_tab_2)
                    tab2view = tab.view
                }
            }
        }
        val tabLayoutMediator = TabLayoutMediator(tab_layout, view_pager, tabLayoutStrategy)
        tabLayoutMediator.attach()

        for (i in 0 until tab_layout.tabCount) {
            val tab: TabLayout.Tab? = tab_layout.getTabAt(i)
            if (tab != null) {
                val tabTextView = TextView(this)
                tab.customView = tabTextView
                tabTextView.layoutParams.width = WRAP_CONTENT
                tabTextView.layoutParams.height = WRAP_CONTENT
                tabTextView.text = tab.text
                if (i == 0) {
                    tabTextView.textSize = 16f
                    tabTextView.typeface = Typeface.DEFAULT_BOLD
                }
                tabTextView.setTextColor(
                    ContextCompat.getColor(
                        this@DetectedTextActivity,
                        android.R.color.white
                    )
                )
            }
        }

        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                Timber.i("onTabSelected called")
                val vg = tab_layout.getChildAt(0) as (ViewGroup)
                val vgTab = vg.getChildAt(tab.position) as ViewGroup
                val tabChildCount = vgTab.childCount
                for (i in 0 until tabChildCount) {
                    val tabViewChild = vgTab.getChildAt(i)
                    if (tabViewChild is TextView) {
                        tabViewChild.textSize = 16f
                        tabViewChild.setTextColor(
                            ContextCompat.getColor(
                                this@DetectedTextActivity,
                                android.R.color.white
                            )
                        )
                        tabViewChild.typeface = Typeface.DEFAULT_BOLD
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                Timber.i("onTabUnselected called")
                val vg = tab_layout.getChildAt(0) as ViewGroup
                val vgTab = vg.getChildAt(tab.getPosition()) as ViewGroup
                val tabChildsCount = vgTab.childCount
                for (i in 0 until tabChildsCount) {
                    val tabViewChild = vgTab.getChildAt(i)
                    if (tabViewChild is TextView) {
                        tabViewChild.textSize = 14f
                        tabViewChild.setTextColor(
                            ContextCompat.getColor(
                                this@DetectedTextActivity,
                                android.R.color.white
                            )
                        )
                    }
                }
            }
        })

        view_pager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setPropertiesForTabs(position)
            }
        })
        setListeners()
        setObserver()
        if(Application.isEdit || isSaved){
            fileName = intent.getStringExtra("fileName")?:""
            retry_scan.visibility = View.GONE
            file_name.apply {
                visibility = View.VISIBLE
                text = if(fileName.isEmpty())"No file name" else fileName
            }
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_NAME, "document_edit")
            }
            (application as Application).firebaseAnalytics.logEvent("document_edit",bundle)
        }
    }

    private fun setObserver() {
        Timber.i("setObserver called")
        viewModel.isInProgress.observe(this, androidx.lifecycle.Observer {
            it.let { state ->
                when (state) {
                    is TASK_STATE.SUCCESS -> {
                        Timber.i("succeed")
                        if(state.msg == saveSuccess){
                            showToast(saveSuccess + pubFileLoc)
                            isSaved = true
                            file_name.apply {
                                visibility = View.VISIBLE
                                text = if(fileName.isEmpty())"No file name" else fileName
                            }
                            val bundle = Bundle().apply {
                                putString(FirebaseAnalytics.Param.ITEM_ID, "document_save_success")
                            }
                            (application as Application).firebaseAnalytics.logEvent("document_save_success", bundle)
                        }
                        else showToast(state.msg)

                    }
                    is TASK_STATE.FAILED -> {
                        Timber.i("failed")
                        showToast(state.msg)
                        fileName= intent.getStringExtra("fileName")?:""
                        val bundle = Bundle().apply {
                            putString(FirebaseAnalytics.Param.ITEM_ID, "document_save_failed")
                        }
                        (application as Application).firebaseAnalytics.logEvent("document_save_failed", bundle)
                    }
                    is TASK_STATE.PROGRESS -> {
                        Timber.i("in progress")
                    }
                    else -> {

                    }
                }
            }
        })
    }

    private fun setListeners() {
        Timber.i("setListeners called")
        save_as_doc.setOnClickListener {
            if (checkPermission()) {
                if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
                    showToast("Read only directory. Cannot save file")
                    return@setOnClickListener
                }
                if (Application.isEdit || isSaved) saveFileToInternalDir(fileName, Constants.DOCX_EXT, "")
                else showFileNameDialog()
            } else requestPermission()
        }
        share.setOnClickListener {
            try {
                shareText(Utility.detectedText)
            }catch (ex:Exception){
                showToast("Sharing failed. Please try again")
            }
        }
        retry_scan.setOnClickListener {
            onBackPressed()
        }
    }

    private fun checkPermission(): Boolean {
        Timber.i("checkPermission called")
        val result = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        );
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        Timber.i("requestPermission called")
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Toast.makeText(
                this,
                "Write External Storage permission allows us to create files. Please allow this permission in App Settings.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1001
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.i("onRequestPermissionsResult called")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.e("Permission Granted, Now you can use local drive .")
            } else {
                Timber.e("Permission Denied, You cannot use local drive .")
            }
        }
    }

    private fun setPropertiesForTabs(position: Int) {
        Timber.i("setPropertiesForTabs at $position called")
        when (position) {
            0 -> {
                tab1view.background =
                    ContextCompat.getDrawable(this, R.drawable.shape_rectangle_with_border)
                tab2view.background = null
                edit_btn.visibility = View.VISIBLE
            }
            1 -> {
                tab2view.background =
                    ContextCompat.getDrawable(this, R.drawable.shape_rectangle_with_border)
                tab1view.background = null
                edit_btn.visibility = View.GONE
            }
            2 -> {
                tab2view.background = null
                tab1view.background = null
                edit_btn.visibility = View.GONE
            }
        }
    }

    private fun showFileNameDialog() {
        Timber.i("showFileNameDialog called")
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.layout_filename_input_dialog)
        val params: ViewGroup.LayoutParams = dialog.window!!.attributes
        params.height = WRAP_CONTENT
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        dialog.setCancelable(false)
        dialog.findViewById<MaterialButton>(R.id.dialog_neg_btn).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<MaterialButton>(R.id.dialog_pos_btn).apply {
           setOnClickListener {
               dialog.findViewById<TextInputLayout>(R.id.et_container).also {
                   val fileName = it.editText?.text.toString().trim()
                   if (fileName.isNullOrEmpty()) {
                       it.isErrorEnabled = true
                       it.error = "Please provide file name"
                       return@setOnClickListener
                   }
                   if (Application.docFilenames.contains(fileName)) {
                       Timber.i("file name already found")
                       it.isErrorEnabled = true
                       it.error = "$fileName already exists. Please provide different name "
                       return@setOnClickListener
                   }else{
                       Timber.i("saving image")
                    val imagePath =  writeImageInInternal(Constants.IMAGES_DIR,"IMG_${getCurrentTime()}.jpg",
                           Utility.bitmap
                       )
                       if(imagePath==null) showToast("Something went wrong when saving your file. Please try agin")
                       else {
                           Timber.i("saving file")
                           saveFileToInternalDir(fileName, Constants.DOCX_EXT,imagePath)
                           dialog.dismiss()
                           this@DetectedTextActivity.fileName = fileName
                       }
                   }
               }
           }
        }
        dialog.show()
    }

    private fun saveFileToInternalDir(
        fileName: String,
        ext: String,
        imagePath: String
    ) {
        Timber.i("saveFileToInternalDir called")
        val os = filesDir
        val dir = File(os,DOCUMENTS_DIR)
        val dirExt =  getExternalFilesDir(null)
        if (!dir.exists()) {
            dir.mkdir();
        }
        val file = File(dir, "$fileName.$ext")
        val time = getCurrentTime()
        val scannedDocument = ScannedDocument(fileName, Utility.detectedText, imagePath, file.absolutePath, ext, time)
        viewModel.createDocFile(FileOutputStream(file), Utility.detectedText, scannedDocument)
        if(dirExt==null){
            showToast(saveError)
            return
        }
        dirExt.let{
            if(!it.exists()){
                Timber.i("location created ? ${it.mkdirs()}")
            }
            //val file = File(it)
            val fileLoc = File(it, "$fileName.$ext")
            Timber.i("saving at : ${fileLoc.absolutePath}")
            pubFileLoc = fileLoc.absolutePath
            //if(!fileLoc.exists()) fileLoc.mkdir()
            viewModel.createDocFile(FileOutputStream(fileLoc), Utility.detectedText, scannedDocument,false)
        }
        if(Application.isEdit || isSaved){
            viewModel.updateFileContent(fileName,Utility.detectedText)
        }
    }

    private fun getCurrentTime(): String {
        Timber.i("getCurrentTime called")
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val simpleDateFormat2 = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        val date = Date(System.currentTimeMillis())
        return "${simpleDateFormat.format(date)} at ${simpleDateFormat2.format(date)}"
    }

}