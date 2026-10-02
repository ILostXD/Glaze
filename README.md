<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="shared/src/commonMain/composeResources/drawable/glaze_wordmark.png">
    <img src="docs/assets/glaze-wordmark-dark.png" alt="Glaze" width="300">
  </picture>
  <p><strong>Your files. Your server. Your music.</strong></p>
  <p>An Android player with artwork-driven playback and liquid-glass controls for your Navidrome / Subsonic library.</p>
  <p><a href="https://github.com/ILostXD/Glaze/releases">Download</a> · <a href="#get-started">Setup</a> · <a href="#features">Features</a> · <a href="https://github.com/ILostXD/GlazeCompanion">Companion</a> · <a href="#build-from-source">Build</a></p>
  <img src="docs/screenshots/home.png" alt="Home" width="200">
  <img src="docs/screenshots/artist.png" alt="Artist" width="200">
  <img src="docs/screenshots/playlist.png" alt="Playlist" width="200">
  <img src="docs/screenshots/player.png" alt="Now Playing" width="200">
  <p><sub>Development screenshots. Music and artwork are not included.</sub></p>
</div>

> [!NOTE]
> **Glaze is in alpha.** APKs are currently debug-signed and installed manually. This README describes the current source tree; published releases may lag behind it. You need your own server and music library.

## Get started

You need **Android 8.0 or newer** and an account on a reachable Navidrome or Subsonic-compatible server.

1. Install an APK from [Releases](https://github.com/ILostXD/Glaze/releases).
2. Enter your **music server URL**, username and password.
3. Let the library synchronize, then choose an artist, album or playlist and press Play.

Use HTTPS outside your trusted local network. Saved credentials use Android Keystore-backed encryption; Subsonic requests use salted tokens. Navidrome is the primary integration; other servers depend on their available endpoints.

> [!TIP]
> Existing-library playback does **not** require Companion. Add it for **Jam, server-side downloads and upcoming releases / Pre-Saves**.

## Features

### Browse

- **Home:** artwork shelves, playlists, favorites, recent listening and upcoming releases. Show, hide and reorder shelves.
- **Artist / album pages:** artwork, artist information where available, discography, featured releases and tracklists.
- **Search:** library songs, albums and artists; the Library / Discover selector keeps the same query. Playlist search stays within Playlists. Browse genres when not searching.
- **Favorites:** native server stars for songs, albums and artists. Artist stars also supply the companion’s release-watch list.
- **Playlists:** play, shuffle, add or remove songs, and reorder tracks.
- **Library sync:** pull down at the top of Home or use Settings → Synchronize library. Progress is visible; completed companion downloads refresh the library.

### Listen

- Full-screen **Now Playing**, animated artwork colours and glass controls. The background pauses with playback and adapts to Android rendering capabilities.
- Background playback with notification and lock-screen media controls.
- Queue search and reordering, play-next / add-to-queue actions, shuffle and repeat.
- Optional **Smart shuffle** to favour underplayed songs and reduce recent repeats.
- Server-provided lyrics, with highlighting and seeking when timed lyrics are available.
- Playback speed outside Jam, track information and audio-format details.
- Shareable playback links, Android automation and NFC support.

Listening time is recorded locally per account, excluding pauses and buffering. Plays reaching half the song or four minutes, whichever is shorter, submit a Subsonic scrobble. Pending submissions survive restarts and retry.

### Personalize

Choose light / dark / system theme, a profile picture, glass intensity, mini-player size, navigation style and size, and Home layout. Enable mini-player swipes, long-press song actions and swiping down to close Now Playing; adjust the swipe distance.

### Jam

With [Companion](#add-the-companion), open the song actions in Now Playing and start a Jam. Invite friends with a link or QR code. Guests choose the **host’s phone** or **their own phone** for audio, then add songs, vote and manage the shared queue.

The host can enable or disable guest playback controls and end the session. Guests can leave and change output. Jam continues through the playback service while the UI is closed. Everyone playing audio on their own phone needs access to the **same Navidrome library**. Companion relays state, not audio.

Jam uses 1× playback speed. Remote playback is drift-corrected, not sample-accurate. Nearby discovery is not implemented. Companion restarts end Jams; sessions also expire after 24 hours.

### Discover, upcoming releases and Pre-Saves

- Browse public Deezer catalogue artists, albums and songs, with checks against your existing library.
- Catalogue-only artwork is dimmed and shows download actions. Album details use the normal layout; catalogue IDs are never treated as playable library songs.
- Upcoming albums appear in **On the way** on Home and can replace an artist’s featured release.
- Upcoming details show artwork, a release date, day-based countdown, tracklist preview and **Pre-Save**. Unreleased tracks stay visible but disabled; available library tracks can play.
- Pre-Saves live on Companion per account and trigger the normal download pipeline when due, even with the phone closed. Undo them before release. **Favoriting alone never downloads music.**
- Enable Android’s **Artist releases** notifications for announcements and completed Pre-Saves. Background checks run approximately every 15 minutes when Android permits; notification taps open the release.

Upcoming metadata currently comes from Apple’s public catalogue. The normal cache is six hours, with more frequent checks around release day. Coverage varies and ambiguous artist names are skipped. Dates are calendar dates, not guaranteed launch hours.

## Add the companion

[Glaze Companion](https://github.com/ILostXD/GlazeCompanion) runs alongside Navidrome and slskd, with optional Qobuz support.

1. Follow its [Docker Compose setup guide](https://github.com/ILostXD/GlazeCompanion#setup-with-docker-compose).
2. Give it an HTTPS address, such as `https://companion.example.com`.
3. Save that address in **Glaze → Settings → Companion server**.

Use your **Navidrome URL** at login and **Companion URL** in Settings. Never enter the administrative companion token in the app. Valid users of the configured Navidrome server share downloads and history; favorites and Pre-Saves remain per account. Joining a Jam on another relay does not change your download server.

### Where do downloads go?

Downloads happen on the **server**, not in the phone’s Downloads folder. Configure these existing folders in Companion’s `.env`:

```dotenv
# Host paths: replace these examples with your own directories.
SLSKD_DOWNLOAD_HOST_PATH=/srv/downloads/soulseek/complete
NAVIDROME_LIBRARY_HOST_PATH=/srv/music

# Container paths: keep these with the supplied Compose file.
SLSKD_DOWNLOAD_PATH=/downloads
NAVIDROME_LIBRARY_PATH=/music
```

The first host path must be slskd’s **completed-download folder**; the second must be the music folder Navidrome scans. Compose mounts them inside Companion as `/downloads` and `/music`. slskd may use different container paths, provided both containers see the same underlying folder.

See [directory mapping and permissions](https://github.com/ILostXD/GlazeCompanion#choose-your-download-and-music-folders) for Docker and NAS examples. After editing `.env`, run `docker compose up -d`; a plain restart does not reload environment variables.

## Downloads and release matching

Tap **Download** on a catalogue album or song. Companion searches sources, downloads into its own job folder, checks the files, imports a new artist/release folder and asks Navidrome to rescan. Glaze then refreshes your library.

**Settings → Downloads** shows history, progress and retries. Work continues after closing the sheet or phone app. Settings also shows server storage usage.

| Behaviour | What to expect |
| --- | --- |
| Sources | slskd first by default, then optional Qobuz with your subscription credentials. Order is configurable. |
| Release selection | Conflicting Bonus / Deluxe / other marked editions and obvious compilation paths are excluded before format preference is scored. |
| Song album context | Catalogue song downloads carry the selected album title, so another release is not silently substituted. |
| Import checks | Embedded tags are read before import. Mismatched releases, songs, artists or unwanted compilations are rejected; the existing source fallback can then run. |
| Metadata | **No automatic tag rewriting and no library-wide Smart tagging.** Accepted files retain their downloaded tags. |
| Existing music | Existing destination folders are not overwritten. Removing Smart tagging does not delete old reports or undo previously approved edits. |

Matching uses paths, catalogue information and embedded tags, not audio fingerprinting. Missing or incorrect uploader metadata may prevent import. Downloads add files to your server library; they are **not phone offline caching**.

## Still unfinished

- Home mix cards labelled **Preview** are placeholders; personalized mixes and Stats / Wrapped are unfinished.
- Nearby Jam discovery and sample-accurate synchronized audio are unsupported.
- SpotiFLAC, manual source selection and acquisition cancellation are not implemented.
- Public release coverage is incomplete; Pre-Saving cannot guarantee source availability on release day.

## Troubleshooting

| Problem | Check |
| --- | --- |
| Cannot sign in | Use the Navidrome/Subsonic URL, not Companion. Check credentials, reachability and certificate. |
| New music is missing | Synchronize the library. For downloads, wait for **Added to library**. |
| Downloaded files cannot be found | Check the completed-download mount, slskd directory setting and UID/GID permissions. |
| Wrong-release error | The tags conflict with the request. The candidate is left out of the library instead of renamed to fit. |
| Jam joins but no audio | Check listening output and that the guest can access the same library IDs. |
| No upcoming album | Favorite the artist and check Companion connectivity. Catalogue coverage is incomplete. |
| Delayed release alerts | Check notification permission, connectivity and Android battery restrictions. Android controls job timing. |

## Build from source

Install **JDK 17+**, **Android SDK 36**, and Android Studio or Android command-line tools. Set `ANDROID_HOME`, or use `sdk.dir` in a local `local.properties`.

```sh
git clone https://github.com/ILostXD/Glaze.git
cd Glaze
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest :shared:testDebugUnitTest
```

On Windows, use `.\gradlew.bat` instead of `./gradlew`. Install the result:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

`shared` contains Compose UI and the Subsonic client. `app` contains Android integration, encrypted credentials, notifications and Media3 playback. Server-dependent behaviour needs a reachable account and device to verify.

## Playback links and Android automation

Share actions include links using Navidrome IDs. Write one as an NFC tag’s **URL/URI record**, or open it from an automation app:

```text
glaze://play/playlist/PLAYLIST_ID?shuffle=true&nowPlaying=true
glaze://play/album/ALBUM_ID?shuffle=false&nowPlaying=true
glaze://play/song/SONG_ID?nowPlaying=false
```

`shuffle` defaults to `false` and uses your Smart Shuffle setting. `nowPlaying` defaults to `true`; set it to `false` to keep the library visible. Links use the signed-in account. Sign in first if needed, and leave an active Jam before starting an automation queue. Missing or empty items show an error.

For Tasker or other intent tools, use action `com.glaze.action.PLAY`, package `com.liquidglass`, activity `com.glaze.MainActivity`:

| Extra | Type | Value |
| --- | --- | --- |
| `kind` | String | `song`, `album` or `playlist` |
| `id` | String | Navidrome item ID |
| `shuffle` | Boolean | Optional; defaults to `false` |
| `open_now_playing` | Boolean | Optional; defaults to `true` |

```sh
adb shell am start -a com.glaze.action.PLAY -n com.liquidglass/com.glaze.MainActivity \
  --es kind playlist --es id PLAYLIST_ID --ez shuffle true --ez open_now_playing true
```

The package stays `com.liquidglass` to preserve app data and login across updates. Old `com.liquidglass.MainActivity` / `com.liquidglass.action.PLAY` shortcuts remain compatible. NFC requires an unlocked phone with NFC enabled; physical-tag behaviour has not been verified on every device.

## Project and license

Glaze is developed with AI-assisted coding under human direction. [Report issues](https://github.com/ILostXD/Glaze/issues) with app / Android versions and reproduction steps, never credentials.

Licensed under [GPL-3.0](LICENSE). Glaze is not affiliated with Navidrome, Spotify, Apple, Deezer, Qobuz or the artists shown in screenshots.
