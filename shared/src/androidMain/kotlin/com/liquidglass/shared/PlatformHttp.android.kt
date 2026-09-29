package com.liquidglass.shared

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.security.MessageDigest
import java.security.SecureRandom

internal actual fun platformHttpClient(): HttpClient = HttpClient(OkHttp) {
    expectSuccess = true
}

actual fun saltedToken(password: String): Pair<String, String> {
    val salt = ByteArray(12).also(SecureRandom()::nextBytes)
        .joinToString("") { "%02x".format(it) }
    val hash = MessageDigest.getInstance("MD5")
        .digest((password + salt).toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
    return salt to hash
}
