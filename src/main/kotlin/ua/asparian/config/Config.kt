package ua.asparian.config

import io.github.cdimascio.dotenv.Dotenv

object Config {
    private val dotenv = Dotenv.load()

    val mongoUri: String
        get() = dotenv["MONGO_URI"] ?: throw IllegalStateException("MONGO_URI not found in .env")

    val jwtSecret: String
        get() = dotenv["JWT_SECRET"] ?: throw IllegalStateException("JWT_SECRET not found in .env")

    val passwordEncryptionKey: String
        get() = dotenv["PASSWORD_ENCRYPTION_KEY"] ?: throw IllegalStateException("PASSWORD_ENCRYPTION_KEY not found in .env")
}
