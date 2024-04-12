package com.mixfa.filestorage.serivce

import com.mixfa.filestorage.FileToBigException
import com.mixfa.filestorage.get
import com.mixfa.filestorage.model.StoredFile
import com.mixfa.marketplace.shared.NotFoundException
import com.mixfa.marketplace.shared.fileNotFound
import com.mixfa.marketplace.shared.orThrow
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@Service
class FileStorageService(
    private val filesRepo: StoredFileRepository
) {
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteFile(fileId: String) {
        if (!filesRepo.existsById(fileId)) throw NotFoundException.fileNotFound()

        filesRepo.deleteById(fileId)
    }

    fun getFile(fileId: String): StoredFile {
        return filesRepo.findById(fileId).orThrow()
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun saveFile(file: MultipartFile): StoredFile {
        if (file.size >= MAX_FILE_SIZE_B) throw FileToBigException.get()

        return filesRepo.save(
            StoredFile(
                name = file.name,
                bytes = file.bytes
            )
        )
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun saveFile(fileName: String, uri: String): StoredFile {
        val bytes = URI.create(uri)
            .toURL()
            .openStream().use { stream ->
                if (stream.available() > MAX_FILE_SIZE_B) throw FileToBigException.get()

                stream.readBytes()
            }

        return filesRepo.save(
            StoredFile(
                name = fileName,
                bytes = bytes
            )
        )
    }

    companion object {
        const val MAX_FILE_SIZE_B = 16000000L
    }
}