package ua.asparian.config

object Config {
    val mongoUri: String
        get() = System.getenv("MONGO_URI") ?: throw IllegalStateException("MONGO_URI not set in environment variables")

    val jwtSecret: String
        get() = System.getenv("JWT_SECRET") ?: throw IllegalStateException("JWT_SECRET not set in environment variables")

    val passwordEncryptionKey: String
        get() = System.getenv("PASSWORD_ENCRYPTION_KEY") ?: throw IllegalStateException("PASSWORD_ENCRYPTION_KEY not set in environment variables")
}
