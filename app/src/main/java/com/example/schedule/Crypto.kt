package com.example.schedule

import android.content.Context
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.preference.PreferenceManager
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

fun generateAesKey(): SecretKey =
    KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()


fun rsaPublicStrToKey(stringKey: String): PublicKey {
    val keyBytes: ByteArray = Base64.decode(stringKey, Base64.DEFAULT)
    val spec = X509EncodedKeySpec(keyBytes)
    val keyFactory: KeyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
    return keyFactory.generatePublic(spec)
}

fun getRsaKey(context: Context): PublicKey {
    val mPreference = PreferenceManager.getDefaultSharedPreferences(context)
    val rsaString = mPreference.getString("RSA", "") ?: ""
    return rsaPublicStrToKey(rsaString)
}

fun encryptRsa(message: ByteArray, publicKey: PublicKey): String {
    val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding").apply {
        init(Cipher.ENCRYPT_MODE, publicKey)
    }
    return Base64.encodeToString(cipher.doFinal(message), Base64.DEFAULT)
}