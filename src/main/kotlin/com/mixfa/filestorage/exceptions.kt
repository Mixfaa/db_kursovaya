package com.mixfa.filestorage

import com.mixfa.excify.ExcifyCachedException
import com.mixfa.excify.ExcifyOptionalOrThrow
import com.mixfa.excify.FastThrowable
import com.mixfa.filestorage.model.StoredFile
import com.mixfa.marketplace.shared.NotFoundException

@ExcifyCachedException
class FileToBigException : FastThrowable("File is too large to be stored") {
    companion object
}

@ExcifyCachedException
@ExcifyOptionalOrThrow(type = StoredFile::class, methodName = "orThrow")
val fileNotFound = NotFoundException("File")