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

internal data class JamSaved(val url: String, val apiToken: String, val name: String,
                             val sessionId: String = "", val memberId: String = "",
                             val memberToken: String = "", val inviteToken: String = "")

/** Encrypts the server password with a device-bound Android Keystore key. */
internal class CredentialStore @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("server", Context.MODE_PRIVATE)
    private val alias = "liquidglass-server-credentials"

    fun load(): ServerCredentials? = try {
        val json = loadJson("credentials") ?: return null
        ServerCredentials(json.getString("url"), json.getString("user"), json.getString("password"))
    } catch (_: Exception) { null }

    fun save(credentials: ServerCredentials) {
        val json = JSONObject().put("url", credentials.serverUrl)
            .put("user", credentials.username).put("password", credentials.password)
        saveJson("credentials", json)
    }

    fun loadJam(): JamSaved? = try {
        val json = loadJson("jam") ?: return null
        JamSaved(json.getString("url"), json.getString("apiToken"), json.getString("name"),
            json.optString("sessionId"), json.optString("memberId"), json.optString("memberToken"),
            json.optString("inviteToken"))
    } catch (_: Exception) { null }

    fun saveJam(jam: JamSaved) = saveJson("jam", JSONObject()
        .put("url", jam.url).put("apiToken", jam.apiToken).put("name", jam.name)
        .put("sessionId", jam.sessionId).put("memberId", jam.memberId)
        .put("memberToken", jam.memberToken).put("inviteToken", jam.inviteToken))

    fun clearJamSession() {
        loadJam()?.let { saveJam(it.copy(sessionId = "", memberId = "", memberToken = "", inviteToken = "")) }
    }

    private fun loadJson(entry: String): JSONObject? {
        val saved = preferences.getString(entry, null) ?: return null
        val bytes = Base64.decode(saved, Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return JSONObject(String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8))
    }

    private fun saveJson(entry: String, json: JSONObject) {
        val plain = json.toString().toByteArray(Charsets.UTF_8)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.iv + cipher.doFinal(plain)
        preferences.edit().putString(entry, Base64.encodeToString(encrypted, Base64.NO_WRAP)).apply()
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
