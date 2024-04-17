package com.mixfa.filestorage.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.net.URI

@Document("storedFile")
sealed class StoredFile(
    @Id val id: ObjectId = ObjectId(),
    val name: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoredFile) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    abstract fun bytes(): ByteArray

    class LocallyStored(
        name: String,
        val bytes: ByteArray,
        id: ObjectId = ObjectId()
    ) : StoredFile(id, name) {
        override fun bytes(): ByteArray = bytes
    }

    class ExternallyStored(
        name: String,
        val link: String,
        id: ObjectId = ObjectId()
    ) : StoredFile(id, name) {

        private val bytes: ByteArray by lazy {
            URI.create(link)
                .toURL()
                .readBytes()
        }

        override fun bytes(): ByteArray = bytes

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ExternallyStored) return false
            if (!super.equals(other)) return false

            if (link != other.link) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + link.hashCode()
            return result
        }
    }
}


