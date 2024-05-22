package com.mixfa.filestorage

import com.mixfa.excify.ExcifyCachedException
import com.mixfa.excify.ExcifyOptionalOrThrow
import com.mixfa.excify.FastException
import com.mixfa.filestorage.model.StoredFile
import com.mixfa.shared.NotFoundException

@ExcifyCachedException
@ExcifyOptionalOrThrow(type = StoredFile::class, methodName = "orThrow")
val fileNotFound = NotFoundException("File")