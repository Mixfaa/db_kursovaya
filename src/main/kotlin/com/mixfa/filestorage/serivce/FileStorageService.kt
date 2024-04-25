package com.mixfa.filestorage.serivce

import com.mixfa.filestorage.FileToBigException
import com.mixfa.filestorage.get
import com.mixfa.filestorage.model.StoredFile
import com.mixfa.marketplace.account.service.AccountService
import com.mixfa.marketplace.shared.SecurityUtils
import com.mixfa.marketplace.shared.orThrow
import com.mixfa.marketplace.shared.throwIfNot
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileStorageService(
    private val filesRepo: StoredFileRepository,
    private val accountService: AccountService,
    @Value("\${filestorage.max_file_size}") private val maxFileSize: Long
) {
    @PreAuthorize("hasAuthority('FILES:EDIT')")
    fun deleteFile(fileId: String) {
        val file = filesRepo.findById(fileId).orThrow()

        SecurityUtils.getAuthenticatedPrincipal()
            .throwIfNot(file.owner)

        filesRepo.deleteById(fileId)
    }

    fun getFile(fileId: String): StoredFile = filesRepo.findById(fileId).orThrow()

    @PreAuthorize("hasRole('FILES:EDIT')")
    fun saveFile(file: MultipartFile): StoredFile {
        if (file.size >= maxFileSize) throw FileToBigException.get()
        val account = accountService.getAuthenticatedAccount().orThrow()

        return filesRepo.save(
            StoredFile.LocallyStored(
                name = file.name, bytes = file.bytes, owner = account
            )
        )
    }

    @PreAuthorize("hasRole('FILES:EDIT')")
    fun saveFile(fileName: String, uri: String): StoredFile {
        val account = accountService.getAuthenticatedAccount().orThrow()
        return filesRepo.save(
            StoredFile.ExternallyStored(
                name = fileName, link = uri, owner = account
            )
        )
    }
}
