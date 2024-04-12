package com.mixfa.filestorage.serivce

import com.mixfa.filestorage.model.StoredFile
import org.springframework.data.mongodb.repository.MongoRepository

interface StoredFileRepository : MongoRepository<StoredFile, String>