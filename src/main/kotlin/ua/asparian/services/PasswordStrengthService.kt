package ua.asparian.services

import com.nulabinc.zxcvbn.Zxcvbn
import com.nulabinc.zxcvbn.Strength

class PasswordStrengthService {
    private val zxcvbn = Zxcvbn()

    fun evaluatePasswordStrength(password: String): Map<String, Any> {
        val strength: Strength = zxcvbn.measure(password)

        return mapOf(
            "score" to strength.score, // Оцінка (0–4)
            "warning" to (strength.feedback.warning ?: ""), // Попередження
            "suggestions" to strength.feedback.suggestions, // Рекомендації
            "crackTimeOfflineSlowHashingSeconds" to strength.crackTimeSeconds.offlineSlowHashing1e4perSecond, // Час у секундах
            "crackTimeOfflineFastHashingSeconds" to strength.crackTimeSeconds.offlineFastHashing1e10PerSecond, // Час у секундах
            "crackTimeOfflineSlowHashingDisplay" to strength.crackTimesDisplay.offlineSlowHashing1e4perSecond, // Час у вигляді тексту
            "crackTimeOfflineFastHashingDisplay" to strength.crackTimesDisplay.offlineFastHashing1e10PerSecond // Час у вигляді тексту
        )
    }
}
