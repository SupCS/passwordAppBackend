package ua.asparian.services
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

class EncryptionService(secret: String) {
    private val algorithm = "AES"
    private val key: Key = SecretKeySpec(secret.toByteArray(), algorithm)
    private val cipher: Cipher = Cipher.getInstance(algorithm)

    fun encrypt(data: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(data: String): String {
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedBytes = Base64.getDecoder().decode(data)
        return String(cipher.doFinal(decodedBytes))
    }
}
