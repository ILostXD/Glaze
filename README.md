# Glaze

Android-first Navidrome/Subsonic music client. The shared module contains the API client and Compose Multiplatform UI; the Android app owns credential storage and Media3 playback.

## Build

Open this folder in Android Studio, or run `gradlew.bat :app:assembleDebug` on Windows. The project requires Android SDK 36 and JDK 17 or newer. Install the resulting `app/build/outputs/apk/debug/app-debug.apk` on an Android 8.0+ device.

On first launch, enter your Navidrome base URL, username, and password. The app validates the connection with `ping`, then stores credentials encrypted with Android Keystore. Use HTTPS when the server is reachable beyond a trusted local network. Each Subsonic call uses a fresh salt and MD5 token as required by the API.

## Current milestone

The app connects to a Subsonic-compatible server, browses and searches music, plays through a background Media3 service, and includes a Now Playing screen with lyrics, queue editing, and shuffle. The interface and playback behavior are still being polished; this is a development build.

## Checks

Run `gradlew.bat :app:testDebugUnitTest :shared:testDebugUnitTest` for local tests. Live server playback needs a reachable Navidrome account and an Android device or emulator.

API behavior follows the [Subsonic API reference](https://www.subsonic.org/pages/api.jsp). Background playback follows [Media3's service guidance](https://developer.android.com/media/media3/session/background-playback).
