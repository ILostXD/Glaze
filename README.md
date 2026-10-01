<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="shared/src/commonMain/composeResources/drawable/glaze_wordmark.png">
    <img src="docs/assets/glaze-wordmark-dark.png" alt="Glaze" width="300">
  </picture>

  <p>Your self-hosted music library, beautifully yours.</p>

  <p>
    <a href="https://github.com/ILostXD/Glaze/releases">Download the latest alpha</a>
    · <a href="#build-from-source">Build from source</a>
    · <a href="https://github.com/ILostXD/Glaze/issues">Report an issue</a>
  </p>
</div>

Glaze is an Android music player for [Navidrome](https://www.navidrome.org/) and other Subsonic-compatible servers. Browse the library you host, explore artists and albums, and listen with a player designed around the music's artwork. Glaze is a client: you need your own server and music library.

<div align="center">
  <img src="docs/screenshots/home.png" alt="Glaze home screen" width="200">
  <img src="docs/screenshots/artist.png" alt="Artist page" width="200">
  <img src="docs/screenshots/playlist.png" alt="Playlist page" width="200">
  <img src="docs/screenshots/player.png" alt="Now Playing screen" width="200">
</div>

<p align="center"><sub>Home · Artist · Playlist · Now Playing — captured from a development build. Music and artwork shown are not included with Glaze.</sub></p>

> [!NOTE]
> Glaze is in alpha. Expect rough edges, and use the [pre-release APKs](https://github.com/ILostXD/Glaze/releases) for testing rather than as a production-critical player. Current alpha APKs are debug-signed and installed manually.

## What you can do

- Browse your Subsonic-compatible library; global search covers songs, albums and artists. Playlist search stays within Playlists.
- Play in the background with Android media controls; manage the queue, shuffle, and view lyrics when available.
- Explore artwork-led album and artist pages, favorite music, and edit playlist order.
- Choose light or dark mode and tune the mini-player, navigation, glass intensity, and gestures in Settings.
- Start a Jam from Now Playing’s song actions to share a queue with friends on the same Navidrome library.
- Search beside your profile and switch **Library / Discover** below it without retyping your query. Both result lists use matching glass cards and section headings. Discover browses released Deezer catalogue albums and songs not in your server library, with darker, borderless colour artwork and a download overlay. It uses the regular album page with the normal-colour **Download** button and per-track download icons instead of playback actions. Search song menus use the app's swipe-dismissible actions sheet.
- Pull down at the top of **Home** to refresh, or use **Settings → Synchronize library** to see synchronization progress. Completed downloads automatically refresh the library, even after dismissing their status sheet.
- A theme-coloured loading ring around your profile picture indicates active requests. Open **Settings → Downloads** for download history and status.

## Get started

1. Install the APK from the [latest GitHub release](https://github.com/ILostXD/Glaze/releases) on an Android 8.0 (API 26) or newer device.
2. Open Glaze and enter your server URL, username, and password.
3. Start browsing your library. Use an **HTTPS** server URL when connecting over the internet; plain HTTP should only be used on a network you trust.

Glaze checks the connection before saving your account. Credentials are stored using Android Keystore-backed encryption, and Subsonic requests use salted token authentication. The app does not provide a music catalog or host your files.

Jam and Discover downloads need a separate [Glaze Companion](https://github.com/ILostXD/GlazeCompanion) server over HTTPS. Save its address in **Settings → Companion server**. Joining an invitation on another relay does not change your library-download server. Jam hosts and library-acquisition requests use salted authentication from the configured Navidrome account; the companion admin token is never stored in the phone app. Guests join using a shared link or QR code and play music through their own Navidrome connection, so everyone needs access to the same library. Jam runs with the playback service while the UI is closed, supports queue voting and applies guest permissions to Android media controls. Playback speed stays at 1× with repeat and shuffle disabled during Jam. Nearby discovery is not implemented.

Listening time is logged locally per server/account, excluding pauses and buffering. Smart shuffle uses recent track IDs. Plays reaching half the track duration or four minutes, whichever is shorter, submit a Subsonic scrobble from any queue, including playlists; pending submissions persist per account and retry until acknowledged. Stats/Wrapped screens remain unfinished.

Discover downloads search configured sources in priority order (slskd by default, optionally Qobuz with a subscription). The companion—not your phone—downloads, moves and rescans files. Status in the sheet progresses through searching, downloading, moved and rescanned, or shows a failure. Active downloads remain tracked after dismissing the sheet and refresh the phone library once rescanned. Only the Navidrome owner configured on the companion can submit jobs. SpotiFLAC is not integrated yet.

Discover uses public Deezer metadata directly on the phone; it sends only the selected artist/title and request kind to the existing companion acquisition endpoint. It verifies ownership against all library albums and matching local tracks, including collaborative artist credits, shows up to 20 catalogue search matches per category, and never plays catalogue IDs or previews as library music.

Artist favorites also follow releases: the companion reads Navidrome’s native artist stars, with no separate Follow button or follow list. Upcoming albums appear in **On the way**, the second Home shelf by default, and replace the featured album on artist pages. The existing album layout shows artwork, release countdown and a disabled preview of unreleased tracks. **Pre-Save** persists on the companion and submits an album to the normal acquisition pipeline on release day, even with the phone closed; favoriting alone never downloads music. You can undo a Pre-Save before release.

Allow Android’s **Artist releases** notifications for announcements and completed Pre-Saves. A persisted native JobScheduler job checks while the app is closed, approximately every 15 minutes when Android permits network work; tapping a notification opens the release. Notifications are deduplicated per Navidrome account. Upcoming metadata comes from Apple’s public catalogue on the companion, skips ambiguous artist names, and refreshes every six hours. Catalogue timestamps drive the countdown; the exact availability time and catalogue coverage can vary by territory. Public catalogue tracks use acquisition actions, never fake playback IDs.

## Build from source

You need JDK 17 or newer, Android SDK 36, and an Android device or emulator. Open the project in Android Studio, or use the Gradle wrapper:

```powershell
.\gradlew.bat :app:assembleDebug
```

On macOS or Linux, use `./gradlew :app:assembleDebug`. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

Run the local tests with:

```powershell
.\gradlew.bat :app:testDebugUnitTest :shared:testDebugUnitTest
```

Live playback and server-dependent behavior require a reachable Navidrome/Subsonic account and an Android device or emulator.

The project has two modules: `shared` holds the Subsonic client and Compose UI; `app` provides Android credential storage and the Media3 playback service.

## Playback links and Android automation

Song, album and playlist Share actions include a playback link using the item's Navidrome ID.
Write just the link as an NFC tag's **URL/URI record**, or open it from an automation app:

```text
glaze://play/playlist/PLAYLIST_ID?shuffle=true&nowPlaying=true
glaze://play/album/ALBUM_ID?shuffle=false&nowPlaying=true
glaze://play/song/SONG_ID?nowPlaying=false
```

`shuffle` defaults to `false`; `nowPlaying` defaults to `true`. Set `nowPlaying=false` to keep
the library view instead of opening the player. Shuffle uses your existing Smart Shuffle setting.
Links use the account already signed into Glaze. If signed out, sign in to continue the request.
Leave an active Jam before starting an automation queue. Empty or missing library items show an error.

For Tasker or another app that sends Android intents, use action `com.glaze.action.PLAY`,
package `com.liquidglass`, activity `com.glaze.MainActivity`, and these extras:

| Extra | Type | Value |
| --- | --- | --- |
| `kind` | String | `song`, `album`, or `playlist` |
| `id` | String | The Navidrome item ID from its playback link |
| `shuffle` | Boolean | Optional; defaults to `false` |
| `open_now_playing` | Boolean | Optional; defaults to `true` |

The install package remains `com.liquidglass` so updates preserve your data and saved login.
The old `com.liquidglass.MainActivity` component and `com.liquidglass.action.PLAY` action remain
compatible with existing shortcuts. New automations should use the Glaze activity and action above.

Example command (replace `PLAYLIST_ID`):

```sh
adb shell am start -a com.glaze.action.PLAY -n com.liquidglass/com.glaze.MainActivity \
  --es kind playlist --es id PLAYLIST_ID --ez shuffle true --ez open_now_playing true
```

NFC must be enabled and the phone unlocked. A physical NFC tag was not tested during development.

## AI-assisted development

Glaze was made with **AI-assisted coding**. AI tools have helped implement and refine code, tests, and documentation under human direction. As with any alpha software, review the source and test a build before relying on it.

## License

Glaze is licensed under the [GNU General Public License v3.0](LICENSE). It is not affiliated with Navidrome or the artists whose artwork may appear in screenshots.
