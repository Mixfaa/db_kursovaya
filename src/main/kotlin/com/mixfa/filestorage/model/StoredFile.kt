package com.mixfa.filestorage.model

import com.mixfa.marketplace.account.model.Account
import com.mixfa.marketplace.shared.defaultLazy
import com.mixfa.marketplace.shared.model.WithDto
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.net.URI

@Document("storedFile")
sealed class StoredFile(
    @Id val id: ObjectId = ObjectId(), val name: String, @DBRef val owner: Account
) : WithDto {

    @delegate:Transient
    override val asDto: Dto by lazy { Dto(this) }

    data class Dto(
        val id: String, val name: String, val ownerId: String
    ) {
        constructor(file: StoredFile) : this(file.id.toString(), file.name, file.owner.username)
    }

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
        name: String, val bytes: ByteArray, owner: Account, id: ObjectId = ObjectId()
    ) : StoredFile(id, name, owner) {
        override fun bytes(): ByteArray = bytes
    }

    class ExternallyStored(
        val link: String, name: String, owner: Account, id: ObjectId = ObjectId()
    ) : StoredFile(id, name, owner) {

        private val bytes: ByteArray by defaultLazy {
            URI.create(link).toURL().readBytes()
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

    class ImgurStored(
        val link: String,
        val deleteHash: String,
        val imgurId: String,
        name: String,
        owner: Account,
        id: ObjectId = ObjectId()
    ) : StoredFile(id, name, owner) {

        private val bytes: ByteArray by defaultLazy {
            URI.create(link).toURL().readBytes()
        }

        override fun bytes(): ByteArray = bytes
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ImgurStored) return false
            if (!super.equals(other)) return false

            if (link != other.link) return false
            if (deleteHash != other.deleteHash) return false
            if (imgurId != other.imgurId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + link.hashCode()
            result = 31 * result + deleteHash.hashCode()
            result = 31 * result + imgurId.hashCode()
            return result
        }

    }
}


