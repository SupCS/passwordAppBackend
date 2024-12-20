package ua.asparian

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import at.favre.lib.crypto.bcrypt.BCrypt
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.pull
import org.litote.kmongo.push
import ua.asparian.Database
import ua.asparian.config.Config
import ua.asparian.models.SavedPassword
import ua.asparian.models.User
import ua.asparian.services.EncryptionService
import ua.asparian.services.PasswordGenerationRequest
import ua.asparian.services.PasswordService
import com.mongodb.client.model.Updates
import com.mongodb.client.model.Filters
import ua.asparian.services.PasswordStrengthService

fun Application.configureRouting() {
    val passwordService = PasswordService()
    val encryptionService = EncryptionService(Config.passwordEncryptionKey)

    routing {
        // Реєстрація
        post("/register") {
            val request = call.receive<UserRegisterRequest>()

            if (request.username.length < 4) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username must be at least 4 characters long"))
                return@post
            }

            if (request.password.length < 6) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password must be at least 6 characters long"))
                return@post
            }

            val existingUser = Database.usersCollection.find(User::username eq request.username).firstOrNull()
            if (existingUser != null) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "User already exists"))
                return@post
            }

            val hashedPassword = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())
            val newUser = User(
                id = ObjectId(),
                username = request.username,
                passwordHash = hashedPassword
            )
            Database.usersCollection.insertOne(newUser)
            call.respond(HttpStatusCode.Created, mapOf("message" to "User registered successfully"))
        }

        // Логін
        post("/login") {
            val request = call.receive<UserLoginRequest>()
            val user = Database.usersCollection.find(User::username eq request.username).firstOrNull()
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid username or password"))
                return@post
            }

            val passwordVerified = BCrypt.verifyer().verify(request.password.toCharArray(), user.passwordHash)
            if (!passwordVerified.verified) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid username or password"))
                return@post
            }

            val token = JwtConfig.generateToken(user.username)
            call.respond(HttpStatusCode.OK, mapOf("token" to token, "message" to "Login successful"))
        }


        // Генерація пароля
        post("/generate-password") {
            try {
                val request = call.receive<PasswordGenerationRequest>()
                val password = passwordService.generatePassword(request)
                call.respond(mapOf("password" to password))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        post("/password-strength") {
            val request = call.receive<PasswordStrengthRequest>() // DTO для запиту
            val service = PasswordStrengthService()

            val result = service.evaluatePasswordStrength(request.password)
            call.respond(HttpStatusCode.OK, result)
        }

        authenticate {
            // Збереження пароля
            post("/save-password") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                // Отримання пароля з запиту
                val request = call.receive<SavedPassword>()

                // Додавання шифрування пароля
                val encryptedPassword = encryptionService.encrypt(request.password)

                // Створення нового SavedPassword з унікальним id
                val savedPassword = SavedPassword(
                    id = ObjectId(), // Генеруємо унікальний ObjectId
                    title = request.title,
                    username = request.username,
                    password = encryptedPassword
                )

                // Оновлення колекції юзерів
                val updateResult = Database.usersCollection.updateOne(
                    User::username eq username,
                    push(User::savedPasswords, savedPassword)
                )

                if (updateResult.modifiedCount > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Password saved successfully"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to save password"))
                }
            }

            // Отримання всіх паролів
            get("/saved-passwords") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val user = Database.usersCollection.find(User::username eq username).firstOrNull()
                    ?: return@get call.respond(HttpStatusCode.NotFound, "User not found")

                val decryptedPasswords = user.savedPasswords.map { password ->
                    mapOf(
                        "id" to password.id.toHexString(),
                        "title" to password.title,
                        "username" to password.username,
                        "password" to encryptionService.decrypt(password.password)
                    )
                }

                call.respond(HttpStatusCode.OK, decryptedPasswords)
            }

            // Видалення пароля
            delete("/delete-password/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal?.payload?.getClaim("username")?.asString()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                val passwordId = call.parameters["id"]
                if (passwordId == null || !ObjectId.isValid(passwordId)) {
                    return@delete call.respond(HttpStatusCode.BadRequest, "Invalid password ID")
                }

                val user = Database.usersCollection.find(User::username eq username).firstOrNull()
                    ?: return@delete call.respond(HttpStatusCode.NotFound, "User not found")

                val updateResult = Database.usersCollection.updateOne(
                    Filters.eq("username", username),
                    Updates.pull("savedPasswords", Filters.eq("_id", ObjectId(passwordId)))
                )

                if (updateResult.modifiedCount > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Password deleted successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Password not found"))
                }
            }
        }
    }
}

data class UserRegisterRequest(val username: String, val password: String)
data class PasswordStrengthRequest(val password: String)
data class UserLoginRequest(val username: String, val password: String)
