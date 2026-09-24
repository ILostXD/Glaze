package com.liquidglass

import com.liquidglass.shared.ServerCredentials
import com.liquidglass.shared.Song
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class QueueStoreTest {
    private val account = ServerCredentials("https://music.example.com", "andy", "secret")

    @Test fun preservesShuffleOrderAndCurrentOccurrenceAcrossRestart() {
        val song = Song("A", "Track", "Artist", "Album", isExplicit = true)
        val snapshot = QueueSnapshot(listOf(song, song.copy(id = "B"), song), 1, 42_000L,
            shuffleEnabled = true, unshuffledUpcomingIds = listOf("A", "B", "A"))
        assertEquals(snapshot, QueueStore.decode(QueueStore.encode(account, snapshot), account))
    }

    @Test fun restoresMetadataWithoutSavingCredentialsOrMediaUrls() {
        val snapshot = QueueSnapshot(listOf(Song(
            id = "song-1", title = "Title", artist = "Artist", album = "Album",
            artistId = "artist-1", albumId = "album-1", coverArt = "cover-1",
            durationSeconds = 213, track = 4, genre = "Jazz", playCount = 12,
            created = "2026-09-22T10:00:00Z", starred = true, suffix = "flac",
            samplingRate = 44_100, bitRate = 828,
        )), 0, 12_345L)

        val encoded = QueueStore.encode(account, snapshot)

        assertEquals(snapshot, QueueStore.decode(encoded, account))
        assertFalse(encoded.contains("secret"))
        assertFalse(encoded.contains("/rest/stream.view"))
    }

    @Test fun rejectsOtherAccountsAndBoundsSavedPlaybackState() {
        val snapshot = QueueSnapshot(listOf(Song("song-1", "Title", "Artist", "Album")),
            99, -5L)
        val encoded = QueueStore.encode(account, snapshot)

        assertNull(QueueStore.decode(encoded,
            ServerCredentials("https://other.example.com", "andy", "secret")))
        assertEquals(QueueSnapshot(snapshot.songs, 0, 0L), QueueStore.decode(encoded, account))
        assertNull(QueueStore.decode("{invalid", account))
    }

    @Test fun preservesRepeatedSongsAndSurvivesPasswordChange() {
        val song = Song("song-1", "Title", "Artist", "Album")
        val snapshot = QueueSnapshot(listOf(song, song), 1, 42_000L)
        val encoded = QueueStore.encode(account, snapshot)

        assertEquals(snapshot, QueueStore.decode(encoded,
            ServerCredentials(account.serverUrl, account.username, "new-password")))
        assertNull(QueueStore.decode(encoded,
            ServerCredentials(account.serverUrl, "other-user", account.password)))
    }

    @Test fun discardsIncompleteSongInsteadOfRestoringBrokenMediaItem() {
        val encoded = JSONObject(QueueStore.encode(account,
            QueueSnapshot(listOf(Song("song-1", "Title", "Artist", "Album")), 0, 0L)))
        encoded.getJSONArray("songs").getJSONObject(0).put("id", "")

        assertNull(QueueStore.decode(encoded.toString(), account))
    }

    @Test fun restoresOlderQueuesWithoutNewMetadata() {
        val song = Song("song-1", "Title", "Artist", "Album")
        val encoded = JSONObject(QueueStore.encode(account,
            QueueSnapshot(listOf(song), 0, 0L)))
        encoded.getJSONArray("songs").getJSONObject(0).apply {
            remove("playCount")
            remove("created")
            remove("starred")
            remove("suffix")
            remove("samplingRate")
            remove("bitRate")
        }

        assertEquals(QueueSnapshot(listOf(song), 0, 0L),
            QueueStore.decode(encoded.toString(), account))
    }
}
