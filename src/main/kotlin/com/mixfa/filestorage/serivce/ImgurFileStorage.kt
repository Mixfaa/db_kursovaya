package com.mixfa.filestorage.serivce

import com.fasterxml.jackson.databind.ObjectMapper
import com.mixfa.excify.FastThrowable
import com.mixfa.filestorage.model.ImgurUploadResponse
import com.mixfa.filestorage.model.StoredFile
import com.mixfa.marketplace.account.service.AccountService
import com.mixfa.marketplace.shared.SecurityUtils
import com.mixfa.marketplace.shared.mapBodyTo
import com.mixfa.marketplace.shared.orThrow
import com.mixfa.marketplace.shared.throwIfNot
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers


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

            val deleteRequest = HttpRequest.newBuilder(URI.create("$IMGUR_URL/image/${file.deleteHash}"))
                .DELETE()
                .build()

            webClient.send(deleteRequest, BodyHandlers.discarding())
        }

        filesRepo.deleteById(fileId)
    }

    @PreAuthorize("hasRole('FILES:EDIT')")
    override fun saveFile(file: MultipartFile): StoredFile {
        file.contentType.let { fileType ->
            if (fileType == null || !checkFileType(fileType))
                throw FastThrowable("File type $fileType not supported")
        }

        val uploadRequest = HttpRequest.newBuilder(UPLOAD_URI)
            .POST(HttpRequest.BodyPublishers.ofInputStream { file.inputStream })
            .build()

        val response =
            webClient.send(uploadRequest, BodyHandlers.ofString()).mapBodyTo<ImgurUploadResponse>(objectMapper)

        if (response.status != 200) throw FastThrowable("Imgur error: ${response.status}")
        val account = accountService.getAuthenticatedAccount().orThrow()

        return filesRepo.save(
            StoredFile.ImgurStored(
                name = file.name,
                link = response.data.link,
                owner = account,
                deleteHash = response.data.deleteHash,
                imgurId = response.data.id
            )
        )
    }

    companion object {
        const val IMGUR_URL = "https://api.imgur.com/3"
        val UPLOAD_URI: URI = URI("$IMGUR_URL/upload")

        val webClient: HttpClient = HttpClient.newHttpClient()
    }
}
