package com.glaze


import com.glaze.shared.JamPlayback
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class JamRelayTest {
    @Test fun privateEndpointAndClockAnchor() {
        assertEquals("https://andyserver.example.ts.net", validatedJamUrl("https://andyserver.example.ts.net/"))
        assertThrows(IllegalArgumentException::class.java) {
            validatedJamUrl("http://192.168.1.9:8088")
        }
        assertThrows(IllegalArgumentException::class.java) {
            validatedJamUrl("https://andyserver.example.ts.net/?token=secret")
        }
        assertEquals(1_750L, JamPlayback("song", true, 1_000, 10_000, 10_000)
            .targetPosition(10_750))
        assertEquals(1_000L, JamPlayback("song", false, 1_000, 10_000, 10_000)
            .targetPosition(10_750))
    }

    @Test fun parsesRelaySnapshot() {
        val json = JSONObject("""{
            "id":"jam","host_id":"host","revision":3,"server_time_ms":10000,
            "guest_playback":true,
            "members":[{"id":"host","name":"Andy"}],
            "queue":[{"id":"item","track_id":"song","added_by":"host","votes":{"host":true}}],
            "playback":{"track_id":"song","playing":true,"position_ms":1200,"updated_at_ms":9000,"shuffle":true,"repeat":2}
        }""")
        val snapshot = parseJamSnapshot(json)
        assertEquals("Andy", snapshot.members.single().name)
        assertEquals(true, snapshot.guestPlayback)
        assertEquals(true, snapshot.playback.shuffle)
        assertEquals(2, snapshot.playback.repeat)
        assertEquals(setOf("host"), snapshot.queue.single().voters)
        assertEquals(2_200L, snapshot.playback.targetPosition(10_000))
    }

    @Test fun inviteCodeIsStrict() {
        val id = "a".repeat(32)
        val secret = "b".repeat(64)
        assertEquals(id to secret, parseJamInvite("$id:$secret"))
        assertEquals(null, parseJamInvite("$id:${"z".repeat(64)}"))
        assertEquals(null, parseJamInvite("$id:$secret:extra"))
        val link = jamInviteLink("https://jam.example.org", id, secret)
        assertEquals("https://jam.example.org" to "$id:$secret", parseJamInviteLink(link))
        assertEquals(null, parseJamInviteLink("glaze://jam/join?server=http%3A%2F%2Fevil.org&code=$id:$secret"))
    }
}
