package qr

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.ByteBuffer

/**
 * QR Code Encryption Utility
 * 
 * Task 73: Add support for encrypted QR codes
 * 
 * Provides AES-GCM encryption for QR code content with secure key derivation
 * and tamper-resistant encrypted payloads.
 */
class QRCodeEncryption {
    
    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        private const val KEY_LENGTH = 256
        
        // Prefix to identify encrypted QR codes
        private const val ENCRYPTED_PREFIX = "ENC:"
    }
    
    /**
     * Configuration for QR code encryption
     */
    data class EncryptionConfig(
        val key: SecretKey? = null,
        val password: String? = null,
        val includeMetadata: Boolean = true,
        val compressionEnabled: Boolean = true
    )
    
    /**
     * Result of encryption operation
     */
    data class EncryptionResult(
        val success: Boolean,
        val encryptedContent: String? = null,
        val key: SecretKey? = null,
        val error: String? = null,
        val metadata: Map<String, Any> = emptyMap()
    )
    
    /**
     * Result of decryption operation
     */
    data class DecryptionResult(
        val success: Boolean,
        val decryptedContent: String? = null,
        val error: String? = null,
        val metadata: Map<String, Any> = emptyMap()
    )
    
    /**
     * Generate a new AES encryption key
     */
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_LENGTH)
        return keyGenerator.generateKey()
    }
    
    /**
     * Derive a key from a password using PBKDF2
     */
    fun deriveKeyFromPassword(password: String, salt: ByteArray = generateSalt()): SecretKey {
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt, 100000, KEY_LENGTH)
        val key = factory.generateSecret(spec)
        return SecretKeySpec(key.encoded, ALGORITHM)
    }
    
    /**
     * Generate a random salt for key derivation
     */
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }
    
    /**
     * Encrypt content for QR code
     */
    fun encrypt(content: String, config: EncryptionConfig = EncryptionConfig()): EncryptionResult {
        return try {
            val key = config.key ?: config.password?.let { 
                deriveKeyFromPassword(it) 
            } ?: generateKey()
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val iv = cipher.iv
            val contentBytes = if (config.compressionEnabled) {
                compressString(content)
            } else {
                content.toByteArray(Charsets.UTF_8)
            }
            
            val encryptedBytes = cipher.doFinal(contentBytes)
            
            // Create the payload: IV + encrypted content
            val payload = ByteBuffer.allocate(iv.size + encryptedBytes.size)
            payload.put(iv)
            payload.put(encryptedBytes)
            
            val encryptedContent = ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(payload.array())
            
            val metadata = mutableMapOf<String, Any>(
                "algorithm" to TRANSFORMATION,
                "keyLength" to KEY_LENGTH,
                "ivLength" to iv.size,
                "originalLength" to content.length,
                "encryptedLength" to encryptedContent.length,
                "compressionEnabled" to config.compressionEnabled
            )
            
            if (config.includeMetadata) {
                metadata["timestamp"] = System.currentTimeMillis()
                metadata["version"] = "1.0"
            }
            
            EncryptionResult(
                success = true,
                encryptedContent = encryptedContent,
                key = key,
                metadata = metadata
            )
        } catch (e: Exception) {
            EncryptionResult(
                success = false,
                error = "Encryption failed: ${e.message}"
            )
        }
    }
    
    /**
     * Decrypt QR code content
     */
    fun decrypt(encryptedContent: String, key: SecretKey): DecryptionResult {
        return try {
            if (!encryptedContent.startsWith(ENCRYPTED_PREFIX)) {
                return DecryptionResult(
                    success = false,
                    error = "Content is not encrypted or uses unsupported encryption format"
                )
            }
            
            val encodedPayload = encryptedContent.substring(ENCRYPTED_PREFIX.length)
            val payload = Base64.getDecoder().decode(encodedPayload)
            
            if (payload.size < GCM_IV_LENGTH) {
                return DecryptionResult(
                    success = false,
                    error = "Invalid encrypted payload: too short"
                )
            }
            
            val buffer = ByteBuffer.wrap(payload)
            val iv = ByteArray(GCM_IV_LENGTH)
            buffer.get(iv)
            
            val encryptedBytes = ByteArray(buffer.remaining())
            buffer.get(encryptedBytes)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            
            // Try to decompress if it looks like compressed data
            val decryptedContent = try {
                decompressString(decryptedBytes)
            } catch (e: Exception) {
                // If decompression fails, treat as regular string
                String(decryptedBytes, Charsets.UTF_8)
            }
            
            val metadata = mapOf<String, Any>(
                "algorithm" to TRANSFORMATION,
                "originalLength" to decryptedContent.length,
                "encryptedLength" to encryptedContent.length,
                "decryptionTime" to System.currentTimeMillis()
            )
            
            DecryptionResult(
                success = true,
                decryptedContent = decryptedContent,
                metadata = metadata
            )
        } catch (e: Exception) {
            DecryptionResult(
                success = false,
                error = "Decryption failed: ${e.message}"
            )
        }
    }
    
    /**
     * Decrypt using password-derived key
     */
    fun decryptWithPassword(encryptedContent: String, password: String): DecryptionResult {
        return try {
            val key = deriveKeyFromPassword(password)
            decrypt(encryptedContent, key)
        } catch (e: Exception) {
            DecryptionResult(
                success = false,
                error = "Password-based decryption failed: ${e.message}"
            )
        }
    }
    
    /**
     * Check if content is encrypted
     */
    fun isEncrypted(content: String): Boolean {
        return content.startsWith(ENCRYPTED_PREFIX)
    }
    
    /**
     * Export key to Base64 string for storage/transmission
     */
    fun exportKey(key: SecretKey): String {
        return Base64.getEncoder().encodeToString(key.encoded)
    }
    
    /**
     * Import key from Base64 string
     */
    fun importKey(encodedKey: String): SecretKey {
        val keyBytes = Base64.getDecoder().decode(encodedKey)
        return SecretKeySpec(keyBytes, ALGORITHM)
    }
    
    /**
     * Simple compression using GZIP
     */
    private fun compressString(input: String): ByteArray {
        val baos = java.io.ByteArrayOutputStream()
        val gzipOut = java.util.zip.GZIPOutputStream(baos)
        gzipOut.write(input.toByteArray(Charsets.UTF_8))
        gzipOut.close()
        return baos.toByteArray()
    }
    
    /**
     * Simple decompression using GZIP
     */
    private fun decompressString(compressed: ByteArray): String {
        val bais = java.io.ByteArrayInputStream(compressed)
        val gzipIn = java.util.zip.GZIPInputStream(bais)
        val result = gzipIn.readBytes()
        gzipIn.close()
        return String(result, Charsets.UTF_8)
    }
}
