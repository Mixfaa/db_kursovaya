package com.mixfa.filestorage.serivce

import com.fasterxml.jackson.databind.ObjectMapper
import com.mixfa.excify.FastException
import com.mixfa.filestorage.model.ImgurUploadResponse
import com.mixfa.filestorage.model.StoredFile
import com.mixfa.account.service.AccountService
import com.mixfa.shared.*
import org.springframework.beans.factory.annotation.Value
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

    override fun deleteFile(fileId: String) {
        val file = filesRepo.findById(fileId).orThrow()

        authenticatedPrincipal()
            .throwIfNot(file.owner)

        if (file is StoredFile.ImgurStored) {
            val deleteRequest = HttpRequest.newBuilder(URI("$IMGUR_URL/image/${file.deleteHash}"))
                .DELETE()
                .build()

            webClient.send(deleteRequest, BodyHandlers.discarding())
        }

        filesRepo.deleteById(fileId)
    }

    override fun saveFile(file: MultipartFile): StoredFile {
        file.contentType.let { fileType ->
            if (fileType == null || !checkFileType(fileType))
                throw FastException("File type $fileType not supported")
        }

        val account = accountService.getAuthenticatedAccount().orThrow()

        return runOrNull {
            val uploadRequest = HttpRequest.newBuilder(URI("$IMGUR_URL/upload"))
                .POST(HttpRequest.BodyPublishers.ofInputStream { file.inputStream })
                .build()

            val response = webClient.send(uploadRequest, BodyHandlers.ofString())

            response.statusCode().let { statusCode ->
                if (!statusCode.httpSuccessful())
                    throw FastException("Imgur returned error: $statusCode ${response.body()}")
            }

            val uploadResponse = response.mapBodyTo<ImgurUploadResponse>(objectMapper)

            filesRepo.save(
                StoredFile.ImgurStored(
                    name = file.name,
                    link = uploadResponse.data.link,
                    owner = account,
                    deleteHash = uploadResponse.data.deleteHash,
                    imgurId = uploadResponse.data.id
                )
            )
        } ?: super.saveFile(file) // saving file in normal way
    }

    companion object {
        const val IMGUR_URL = "https://api.imgur.com/3"
        val webClient: HttpClient = HttpClient.newHttpClient()
    }
}
