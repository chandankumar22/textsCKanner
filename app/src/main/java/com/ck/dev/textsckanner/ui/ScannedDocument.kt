package com.ck.dev.textsckanner.ui

data class ScannedDocument(
    var documentName: String,
    val documentTitle: String?,
    val imgPath: String?,
    val docPath: String?,
    val extension: String?,
    val creationDate: String?,
    var isSelected: Boolean = false,
    var size: Float = 0f
)