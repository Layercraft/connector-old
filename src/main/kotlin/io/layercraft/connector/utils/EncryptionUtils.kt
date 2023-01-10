package io.layercraft.connector.utils

import io.layercraft.connector.SERVERID
import java.math.BigInteger
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val SYMMETRIC_ALGORITHM = "AES"
private const val SYMMETRIC_BITS = 128
private const val ASYMMETRIC_ALGORITHM = "RSA"
private const val ASYMMETRIC_BITS = 1024
private const val BYTE_ENCODING = "ISO_8859_1"
private const val HASH_ALGORITHM = "SHA-1"
private const val SIGNING_ALGORITHM = "SHA256withRSA"

class EncryptionUtils {

    private val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(ASYMMETRIC_BITS) }.generateKeyPair()

    val publicKey: RSAPublicKey = keyPair.public as RSAPublicKey
    private val privateKey: RSAPrivateKey = keyPair.private as RSAPrivateKey
    fun generateVerifyToken(): ByteArray = ByteArray(4).apply { (0..3).forEach { this[it] = (0..255).random().toByte() } }

    fun decryptBytesRSA(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keyPair.private)
        return cipher.doFinal(data)
    }

    fun decryptBytesAES(bytes: ByteArray, sharedSecret: SecretKey): ByteArray {
        val cipher = getCipher(Cipher.DECRYPT_MODE, sharedSecret)
        return cipher.update(bytes)
    }

    fun encryptBytesAES(bytes: ByteArray, sharedSecret: SecretKey): ByteArray {
        val cipher = getCipher(Cipher.ENCRYPT_MODE, sharedSecret)
        return cipher.update(bytes)
    }

    private fun getCipher(n: Int, sharedSecret: SecretKey): Cipher {
        val cipher = Cipher.getInstance("AES/CFB8/NoPadding")
        cipher.init(n, sharedSecret, IvParameterSpec(sharedSecret.encoded))
        return cipher
    }

    fun byteArrayToPublicKey(key: ByteArray): PublicKey {
        val keySpec = X509EncodedKeySpec(key)
        val keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM)
        return keyFactory.generatePublic(keySpec)
    }

    fun verifyWithSignature(publicKey: PublicKey, signature: ByteArray, vararg data: ByteArray): Boolean {
        val signatureInstance = Signature.getInstance(SIGNING_ALGORITHM)
        signatureInstance.initVerify(publicKey)
        data.forEach { signatureInstance.update(it) }
        return signatureInstance.verify(signature)
    }

    fun digestData(secretKey: SecretKey): ByteArray {
        val messageDigest = MessageDigest.getInstance(HASH_ALGORITHM)
        messageDigest.update(SERVERID.toByteArray(charset(BYTE_ENCODING)))
        messageDigest.update(secretKey.encoded)
        messageDigest.update(publicKey.encoded)

        return messageDigest.digest()
    }

    // Shared Secret EncryptionBeginPacket
    fun decryptByteToSecretKey(bytes: ByteArray): SecretKey {
        val decryptedBytes = decryptBytesRSA(bytes)
        return SecretKeySpec(decryptedBytes, SYMMETRIC_ALGORITHM)
    }

    fun genSha1Hash(sharedSecret: SecretKey): String {
        return BigInteger(digestData(sharedSecret)).toString(16)
    }

    fun getCipher(n: Int, key: Key): Cipher {
        val cipher = Cipher.getInstance("AES/CFB8/NoPadding")
        cipher.init(n, key, IvParameterSpec(key.encoded))
        return cipher
    }
}
