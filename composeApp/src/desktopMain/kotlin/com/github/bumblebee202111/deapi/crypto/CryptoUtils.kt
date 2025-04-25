package com.github.bumblebee202111.deapi.crypto

import java.security.InvalidKeyException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.SecretKeySpec

internal object CryptoUtils {
    private const val SECRET_KEY_STRING = "e82ckenh8dichen8"
    const val TRANSFORMATION_NAME = "AES/ECB/PKCS5Padding"
    private val secretKeyBytes = SECRET_KEY_STRING.toByteArray()
    private val secretKeySpec = SecretKeySpec(secretKeyBytes, "AES")

    fun decrypt(dataBytes: ByteArray): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION_NAME)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            val decryptedBytes = cipher.doFinal(dataBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            val message = when (e) {
                is BadPaddingException -> "Decryption failed: Invalid padding. Check key/data/corruption."
                is IllegalBlockSizeException -> "Decryption failed: Invalid block size. Check data length."
                is InvalidKeyException -> "Decryption failed: Invalid key. Check key length (16, 24, or 32 bytes)."
                else -> "Decryption failed: ${e.javaClass.simpleName} - ${e.message ?: "Unknown reason"}"
            }
            throw RuntimeException("Decryption failed: $message", e)
        }
    }
}

