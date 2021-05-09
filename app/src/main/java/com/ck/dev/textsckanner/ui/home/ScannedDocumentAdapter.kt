package com.ck.dev.textsckanner.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ck.dev.textsckanner.R
import com.ck.dev.textsckanner.ui.Application
import com.ck.dev.textsckanner.ui.ScannedDocument
import com.ck.dev.textsckanner.ui.detectedtext.DetectedTextActivity
import com.ck.dev.textsckanner.utils.AdapterCallback
import com.ck.dev.textsckanner.utils.Constants
import com.ck.dev.textsckanner.utils.Utility
import com.ck.dev.textsckanner.utils.Utility.bitmap
import com.ck.dev.textsckanner.utils.readImageFileFromInternal
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.list_item_scanned_document.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class ScannedDocumentAdapter(
    private val scannedDoc: ArrayList<ScannedDocument>,
    private var scannedDocListFiltered: ArrayList<ScannedDocument>,
    private val adapterCallback: AdapterCallback
) :
    RecyclerView.Adapter<ScannedDocumentAdapter.ScannedDocumentVH>(), Filterable {

    inner class ScannedDocumentVH(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var isOnclickActivated = false
    private var isToRemove = false
    private var selectedDocIndexed = arrayListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedDocumentVH =
        ScannedDocumentVH(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_scanned_document, parent, false
            )
        )

    override fun getItemCount() = scannedDocListFiltered.size

    fun getSelectedDoc(): ArrayList<ScannedDocument> {
        val listOfSelDocs = ArrayList<ScannedDocument>()
        for (i in 0 until scannedDocListFiltered.size) {
            if (scannedDocListFiltered[i].isSelected) {
                listOfSelDocs.add(scannedDocListFiltered[i])
                selectedDocIndexed.add(i)
            }
        }
        selectedDocIndexed.sortByDescending { it }
        return listOfSelDocs
    }

    fun getSelectedIndex() = selectedDocIndexed

    private fun setUnSelectedDocProperties(
        materialCardView: LinearLayout,
        scannedDocument: ScannedDocument
    ) {
        with(materialCardView) {
            scannedDocument.apply {
                background = AppCompatResources.getDrawable(context, R.drawable.shape_rectangler)
                list_item_root.setPadding(0, 0, 0, 0)
                ic_select.visibility = View.GONE
                //ic_doc_ext.visibility = View.VISIBLE
                isSelected = false
                if (getSelectedDoc().isEmpty()) {
                    isOnclickActivated = false
                    adapterCallback.onItemSelect(false, getSelectedDoc().size)
                } else {
                    adapterCallback.onItemSelect(true, getSelectedDoc().size)
                }
            }
        }
    }

    private fun setSelectedDocProperties(
        materialCardView: LinearLayout,
        scannedDocument: ScannedDocument
    ) {
        with(materialCardView) {
            scannedDocument.apply {
                background = AppCompatResources.getDrawable(context, R.drawable.shape_rectangle_with_border_unselected)
                list_item_root.setPadding(12, 12, 12, 12)
                ic_select.visibility = View.VISIBLE
              //  ic_doc_ext.visibility = View.GONE
                isSelected = true
                adapterCallback.onItemSelect(true, getSelectedDoc().size)
            }
        }
    }

    fun removeAllSelected() {
        scannedDocListFiltered.forEach {
            it.isSelected = false
            isOnclickActivated = false
        }
        isToRemove = true
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ScannedDocumentVH, position: Int) {
        (holder.itemView as LinearLayout).apply {
            Timber.i("scanned document list size selected : ${scannedDocListFiltered.size}")
            with(scannedDocListFiltered[position]) {
                if (!docPath.isNullOrEmpty()) {
                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.CEILING
                    size = df.format((File(docPath).length().toFloat() / 1024)).toFloat()
                    file_size.text = "$size kB"
                    if(size>1024){
                        size /= 1024
                        file_size.text = "$size MB"
                    }
                }
               /* if (!extension.isNullOrEmpty()) {
                    ic_doc_ext.background =
                        if (extension == Constants.DOCX_EXT) ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_list_doc
                        )
                        else ContextCompat.getDrawable(context, R.drawable.ic_list_doc)
                }*/
                if (!imgPath.isNullOrEmpty()) {
                    Glide.with(context).load(imgPath).into(scanned_img)
                }
                file_content.text = if (!documentTitle.isNullOrEmpty()) documentTitle else "No text"
                file_name.text = if (documentName.isNotEmpty()) documentName else "No text"
                if (!creationDate.isNullOrEmpty()) {
                    date.text = creationDate.removeRange(creationDate.length-3,creationDate.length)
                }
                setOnLongClickListener {
                    if (!isOnclickActivated) {
                        isOnclickActivated = true
                        isToRemove = false
                        this@ScannedDocumentAdapter.setSelectedDocProperties(this@apply, this)
                    }
                    true
                }
                setOnClickListener {
                    if (isOnclickActivated) {
                        if (isSelected) {
                            this@ScannedDocumentAdapter.setUnSelectedDocProperties(this@apply, this)
                        } else {
                            this@ScannedDocumentAdapter.setSelectedDocProperties(this@apply, this)
                        }
                    } else {
                        //handle code for edit
                        if (!imgPath.isNullOrEmpty()) {
                            val imgBitmap = context.readImageFileFromInternal(imgPath)
                            if (imgBitmap != null) {
//                            scanned_img.setImageBitmap(imgBitmap)
                                bitmap = imgBitmap
                            }
                        }
                        Application.isEdit = true
                        Utility.detectedText = documentTitle ?: ""
                        val intent = Intent(context, DetectedTextActivity::class.java)
                        intent.putExtra("fileName", documentName ?: "")
                        context.startActivity(intent)
                    }
                }
                if (isSelected) {
                    background = AppCompatResources.getDrawable(context, R.drawable.shape_rectangle_with_border_unselected)
                    list_item_root.setPadding(12, 12, 12, 12)
                    ic_select.visibility = View.VISIBLE
                } else {
                    background = AppCompatResources.getDrawable(context, R.drawable.shape_rectangler)
                    list_item_root.setPadding(0, 0, 0, 0)
                    ic_select.visibility = View.GONE
                }
            }
        }
    }

    fun reset() {
        scannedDocListFiltered = scannedDoc
        notifyDataSetChanged()
      //  isOnclickActivated = false
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                Timber.i("performFiltering called on $charSequence")
                val charString = charSequence.toString()
                if (charString.trim().isEmpty()) {
                    Timber.i("empty query, assigning original doc with length ${scannedDoc.size}")
                    scannedDocListFiltered = scannedDoc
                } else {
                    val filteredList: ArrayList<ScannedDocument> = ArrayList()
                    for (row in scannedDoc) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.documentName.toLowerCase(Locale.getDefault())
                                .contains(charString.toLowerCase(Locale.getDefault())) ||
                            row.documentTitle!!.toLowerCase(Locale.getDefault())
                                .contains(charSequence.toString().toLowerCase(Locale.getDefault()))
                        ) {
                            filteredList.add(row)
                        }
                    }
                    scannedDocListFiltered = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = scannedDocListFiltered
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                Timber.i("publishResults called on $charSequence with length ${(filterResults.values as ArrayList<ScannedDocument>).size}")
                scannedDocListFiltered = filterResults.values as ArrayList<ScannedDocument>
                notifyDataSetChanged()
            }
        }
    }
}
