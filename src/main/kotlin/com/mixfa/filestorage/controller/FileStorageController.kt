package com.mixfa.filestorage.controller

import com.mixfa.filestorage.model.StoredFile
import com.mixfa.filestorage.serivce.FileStorageService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/file-storage")
class FileStorageController(
    private val fsService: FileStorageService
) {
    @PostMapping("/file")
    fun uploadFile(file: MultipartFile) = fsService.saveFile(file)

    @PostMapping("/file_by_url")
    fun uploadFileByUrl(name: String, url: String) = fsService.saveFile(name, url)

    @GetMapping("/file/{fileId}/info")
    fun getFileInfo(@PathVariable fileId: String) = fsService.getFile(fileId)

    @GetMapping("/file/{fileId}/bytes")
    fun getFileBytes(@PathVariable fileId: String) = fsService.getFile(fileId).bytes()

    /**
     * If file is locally stored, returns byte array
     * if file is externally stored, returns link to file
     */
    @GetMapping("/file/{fileId}")
    fun getFile(@PathVariable fileId: String): Any {
        return when (val storedFile = fsService.getFile(fileId)) {
            is StoredFile.ExternallyStored -> storedFile.link
            else -> storedFile.bytes()
        }
    }

    @DeleteMapping("/file/{fileId}")
    fun deleteFile(@PathVariable fileId: String) = fsService.deleteFile(fileId)
}