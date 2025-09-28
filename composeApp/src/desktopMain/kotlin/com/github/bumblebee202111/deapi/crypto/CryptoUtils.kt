package com.github.bumblebee202111.deapi.crypto

import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.SecretKeySpec

internal object CryptoUtils {
    private const val SECRET_KEY_STRING = "e82ckenh8dichen8"
    private const val TRANSFORMATION_NAME = "AES/ECB/PKCS5Padding"
    private val secretKeyBytes = SECRET_KEY_STRING.toByteArray()
    private val secretKeySpec = SecretKeySpec(secretKeyBytes, "AES")

    /**
     * Decrypts EAPI data.
     * @return The raw decrypted bytes.
     */
    fun decrypt(dataBytes: ByteArray): ByteArray {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION_NAME)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            cipher.doFinal(dataBytes)
        } catch (e: Exception) {
            val message = when (e) {
                is BadPaddingException -> "Invalid padding. Check key/data/corruption."
                is IllegalBlockSizeException -> "Invalid block size. Check data length."
                is InvalidKeyException -> "Invalid key specified."
                else -> "${e.javaClass.simpleName} - ${e.message ?: "Unknown reason"}"
            }
            throw RuntimeException("Decryption failed: $message", e)
        }
    }
}