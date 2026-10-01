package com.glaze.shared

import io.ktor.client.HttpClient

internal expect fun platformHttpClient(): HttpClient
expect fun saltedToken(password: String): Pair<String, String>
