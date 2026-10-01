package com.glaze


import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaConstants
import com.glaze.shared.Song
import com.glaze.shared.SubsonicClient

internal fun MediaItem.toSong() = Song(mediaId,
        mediaMetadata.title?.toString() ?: "Unknown title",
        mediaMetadata.artist?.toString() ?: "Unknown artist",
        mediaMetadata.albumTitle?.toString() ?: "Unknown album",
        artistId = mediaMetadata.extras?.getString("artistId"),
        albumId = mediaMetadata.extras?.getString("albumId"),
        coverArt = mediaMetadata.extras?.getString("coverArtId"),
        durationSeconds = mediaMetadata.extras?.getInt("durationSeconds") ?: 0,
        track = mediaMetadata.extras?.let { if (it.containsKey("track")) it.getInt("track") else null },
        genre = mediaMetadata.extras?.getString("genre"),
        playCount = mediaMetadata.extras?.getInt("playCount") ?: 0,
        created = mediaMetadata.extras?.getString("created"),
        starred = mediaMetadata.extras?.getBoolean("starred") ?: false,
        suffix = mediaMetadata.extras?.getString("suffix"),
        samplingRate = mediaMetadata.extras?.let {
            if (it.containsKey("samplingRate")) it.getInt("samplingRate") else null },
        bitRate = mediaMetadata.extras?.let {
            if (it.containsKey("bitRate")) it.getInt("bitRate") else null },
        isExplicit = mediaMetadata.extras?.getBoolean("isExplicit") ?: false)

@OptIn(UnstableApi::class)
internal fun Song.toMediaItem(client: SubsonicClient): MediaItem =
        MediaItem.Builder()
                .setMediaId(id)
                .setUri(client.streamUrl(id))
                .setMediaMetadata(MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(coverArt?.let { Uri.parse(client.coverArtUrl(it)) })
                    .setExtras(Bundle().apply {
                        putString("coverArtId", coverArt)
                        putString("artistId", artistId)
                        putString("albumId", albumId)
                        putInt("durationSeconds", durationSeconds)
                        track?.let { putInt("track", it) }
                        putString("genre", genre)
                        putInt("playCount", playCount)
                        putString("created", created)
                        putBoolean("starred", starred)
                        putString("suffix", suffix)
                        samplingRate?.let { putInt("samplingRate", it) }
                        bitRate?.let { putInt("bitRate", it) }
                        putBoolean("isExplicit", isExplicit)
                        if (isExplicit) putLong(MediaConstants.EXTRAS_KEY_IS_EXPLICIT,
                            MediaConstants.EXTRAS_VALUE_ATTRIBUTE_PRESENT)
                    })
                    .build())
                .build()
