package com.mixfa.filestorage.controller

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

    @PostMapping("/file")
    fun uploadFileByUrl(name: String, url: String) = fsService.saveFile(name, url)

    @GetMapping("/file/{fileId}/info")
    fun getFileInfo(@PathVariable fileId: String) = fsService.getFile(fileId)

    @GetMapping("/file/{fileId}")
    fun getFileBytes(@PathVariable fileId: String) = fsService.getFile(fileId).bytes

    @DeleteMapping("/file/{fileId}")
    fun deleteFile(@PathVariable fileId: String) = fsService.deleteFile(fileId)
}