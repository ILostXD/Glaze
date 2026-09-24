package com.liquidglass

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.liquidglass.shared.ServerCredentials
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

/** Encrypts the server password with a device-bound Android Keystore key. */
internal class CredentialStore @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("server", Context.MODE_PRIVATE)
    private val alias = "liquidglass-server-credentials"

    fun load(): ServerCredentials? = try {
        val saved = preferences.getString("credentials", null) ?: return null
        val bytes = Base64.decode(saved, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        val json = JSONObject(String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8))
        ServerCredentials(json.getString("url"), json.getString("user"), json.getString("password"))
    } catch (_: Exception) { null }

    fun save(credentials: ServerCredentials) {
        val plain = JSONObject().put("url", credentials.serverUrl)
            .put("user", credentials.username).put("password", credentials.password)
            .toString().toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.iv + cipher.doFinal(plain)
        preferences.edit().putString("credentials", Base64.encodeToString(encrypted, Base64.NO_WRAP)).apply()
    }

    fun clear() { preferences.edit().remove("credentials").apply() }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build())
        return generator.generateKey()
    }
}
