package com.example.xolotl.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {

    private const val AES_KEY = "XolotlClaveAES16" // 16 caracteres exactos

    fun encrypt(text: String): String {
        val secretKey = SecretKeySpec(AES_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return Base64.encodeToString(cipher.doFinal(text.toByteArray()), Base64.DEFAULT)
    }
}
