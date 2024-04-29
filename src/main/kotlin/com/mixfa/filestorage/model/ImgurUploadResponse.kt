package com.mixfa.filestorage.model

data class ImgurUploadResponse(
    val status: Int,
    val data: Data
) {
    data class Data(
        val id: String,
        val deleteHash: String,
        val link: String
    )
}