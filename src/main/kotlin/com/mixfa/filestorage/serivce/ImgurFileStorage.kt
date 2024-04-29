package com.mixfa.filestorage.serivce

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mixfa.excify.FastThrowable
import com.mixfa.filestorage.model.ImgurUploadResponse
import com.mixfa.filestorage.model.StoredFile
import com.mixfa.marketplace.account.service.AccountService
import com.mixfa.marketplace.shared.SecurityUtils
import com.mixfa.marketplace.shared.orThrow
import com.mixfa.marketplace.shared.sneakyTry
import com.mixfa.marketplace.shared.throwIfNot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ImgurFileStorage(
    private val filesRepo: StoredFileRepository,
    private val accountService: AccountService,
    @Value("\${filestorage.max_file_size}") private val maxFileSize: Long,
    private val objectMapper: ObjectMapper
) : FileStorageService(filesRepo, accountService, maxFileSize) {

    @PreAuthorize("hasAuthority('FILES:EDIT')")
    override fun deleteFile(fileId: String) {
        val file = filesRepo.findById(fileId).orThrow()

        SecurityUtils.getAuthenticatedPrincipal()
            .throwIfNot(file.owner)

        if (file is StoredFile.ImgurStored) {
            val deleteRequest = Request.Builder()
                .url("$IMGUR_URL/image/${file.deleteHash}")
                .delete()
                .build()

            sneakyTry { webClient.newCall(deleteRequest).execute() }
        }

        filesRepo.deleteById(fileId)
    }

    @PreAuthorize("hasRole('FILES:EDIT')")
    override fun saveFile(file: MultipartFile): StoredFile {
        val fileType = file.contentType
        if (fileType == null || !checkFileType(fileType))
            throw FastThrowable("File type $fileType not supported")

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                file.name, file.originalFilename,
                RequestBody.create("application/octet-stream".toMediaTypeOrNull(), file.bytes)
            )
            .build()

        val postImageRequest = Request.Builder()
            .url("$IMGUR_URL/image")
            .post(requestBody)
            .build()

        val response = webClient.newCall(postImageRequest).execute()
        val body = response.body ?: throw FastThrowable("Error while uploading image to imgur (${response.code})")

        val uploadResponse = objectMapper.readValue<ImgurUploadResponse>(body.string())
        if (uploadResponse.status != 200) throw FastThrowable("Imgur error: ${uploadResponse.status}")
        val account = accountService.getAuthenticatedAccount().orThrow()

        return filesRepo.save(
            StoredFile.ImgurStored(
                name = file.name,
                link = uploadResponse.data.link,
                owner = account,
                deleteHash = uploadResponse.data.deleteHash,
                imgurId = uploadResponse.data.id
            )
        )
    }

    companion object {
        private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        const val IMGUR_URL = "https://api.imgur.com/3"
        val webClient = OkHttpClient()
    }
}
