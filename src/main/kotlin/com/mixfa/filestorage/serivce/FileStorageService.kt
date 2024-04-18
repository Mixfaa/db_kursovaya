package com.mixfa.filestorage.serivce

import com.mixfa.filestorage.FileToBigException
import com.mixfa.filestorage.get
import com.mixfa.filestorage.model.StoredFile
import com.mixfa.marketplace.shared.NotFoundException
import com.mixfa.marketplace.shared.fileNotFound
import com.mixfa.marketplace.shared.orThrow
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.zip.Deflater

@Service
class FileStorageService(
    private val filesRepo: StoredFileRepository,
    @Value("\${filestorage.max_file_size}") private val maxFileSize: Long
) {
    @PreAuthorize("hasAuthority('FILES: WRITE')")
    fun deleteFile(fileId: String) {
        if (!filesRepo.existsById(fileId)) throw NotFoundException.fileNotFound()

        filesRepo.deleteById(fileId)
    }

    @PreAuthorize("hasAuthority('FILES:READ')")
    fun getFile(fileId: String): StoredFile {
        return filesRepo.findById(fileId).orThrow()
    }

    @PreAuthorize("hasRole('FILES:WRITE')")
    fun saveFile(file: MultipartFile): StoredFile {
        if (file.size >= maxFileSize) throw FileToBigException.get()

        return filesRepo.save(
            StoredFile.LocallyStored(
                name = file.name,
                bytes = file.bytes
            )
        )
    }

    @PreAuthorize("hasRole('FILES:WRITE')")
    fun saveFile(fileName: String, uri: String): StoredFile = filesRepo.save(
        StoredFile.ExternallyStored(
            name = fileName,
            link = uri,
        )
    )
}