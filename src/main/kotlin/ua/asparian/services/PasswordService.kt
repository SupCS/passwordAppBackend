package ua.asparian.services

import kotlin.random.Random

data class PasswordGenerationRequest(
    val length: Int,
    val includeUppercase: Boolean,
    val includeLowercase: Boolean,
    val includeNumbers: Boolean,
    val includeSpecialCharacters: Boolean
)

class PasswordService {
    fun generatePassword(request: PasswordGenerationRequest): String {
        if (request.length < 4) throw IllegalArgumentException("Password length must be at least 4 characters ")

        val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val specialCharacters = "!@#$%^&*()-_=+[]{}|;:,.<>?/"

        val characterPool = mutableListOf<Char>()
        if (request.includeUppercase) characterPool.addAll(uppercase.toList())
        if (request.includeLowercase) characterPool.addAll(lowercase.toList())
        if (request.includeNumbers) characterPool.addAll(numbers.toList())
        if (request.includeSpecialCharacters) characterPool.addAll(specialCharacters.toList())

        if (characterPool.isEmpty()) throw IllegalArgumentException("At least one character type must be selected")

        return (1..request.length)
            .map { characterPool.random() }
            .joinToString("")
    }
}
