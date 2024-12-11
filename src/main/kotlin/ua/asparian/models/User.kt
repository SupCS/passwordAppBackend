package ua.asparian.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    @BsonId
    val id: ObjectId = ObjectId(),
    val username: String,
    val passwordHash: String,
    val savedPasswords: List<SavedPassword> = emptyList()
)

data class SavedPassword(
    @BsonId
    val id: ObjectId = ObjectId(),
    val title: String,
    val username: String? = null,
    val password: String
)
