package io.layercraft.connector.utils

import io.layercraft.connector.SERVERID
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {
    private val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(1024) }.genKeyPair()

    val publickey: ByteArray = keyPair.public.encoded
    val privatkey: ByteArray = keyPair.private.encoded

    fun generateVerifyToken(): ByteArray = ByteArray(4).apply { (0..3).forEach { this[it] = (0..255).random().toByte() } }
    //fun generateSharedSecret(bytes: ByteArray): ByteArray = encryptBytes(bytes, keyPair.public)

    fun decryptBytesRSA(bytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, keyPair.private)
        return cipher.doFinal(bytes)
    }

    fun encryptBytesRSA(bytes: ByteArray, key: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(bytes)
    }

    fun decryptBytesAES(bytes: ByteArray, sharedSecret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(sharedSecret, "AES"))
        return cipher.doFinal(bytes)
    }

    fun encryptBytesAES(bytes: ByteArray, sharedSecret: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(sharedSecret, "AES"))
        return cipher.doFinal(bytes)
    }

    fun byteArrayToPublicKey(key: ByteArray, signature: ByteArray): PublicKey {
        val keySpec = X509EncodedKeySpec(key)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(keySpec)

        /*val signatureInstance = Signature.getInstance("SHA256withRSA")
        signatureInstance.initVerify(publicKey)
        signatureInstance.update(key)
        if (!signatureInstance.verify(signature)) {
            throw Exception("Invalid signature")
        }*/

        return publicKey
    }

    fun genSha1Hash(sharedSecret: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-1")
        digest.update(String(SERVERID.toString().toByteArray(Charsets.US_ASCII), Charsets.US_ASCII).substring(0, 20).toByteArray())
        digest.update(sharedSecret)
        digest.update(publickey)
        //digest to hexdigest
        return BigInteger(digest.digest()).toString(16)
    }
}
