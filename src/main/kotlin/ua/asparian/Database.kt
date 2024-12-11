package ua.asparian

import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import ua.asparian.models.User
import ua.asparian.config.Config

object Database {
    private val client = KMongo.createClient(Config.mongoUri)
    private val database = client.getDatabase("password_manager")

    val usersCollection = database.getCollection<User>()
}
