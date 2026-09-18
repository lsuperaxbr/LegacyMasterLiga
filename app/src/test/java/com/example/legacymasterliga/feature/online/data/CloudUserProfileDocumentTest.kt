package com.example.legacymasterliga.feature.online.data

import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.network.OnlineProfileCorruptException
import com.example.legacymasterliga.core.network.OnlineProfileIncompleteException
import com.example.legacymasterliga.feature.online.domain.CloudUserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudUserProfileDocumentTest {

    @Test
    fun `payload has exact Firestore field names and primitive types`() {
        val payload = CloudUserProfileDocument.payload(profile())

        assertEquals(
            setOf(
                "firebaseUid", "localUserId", "username", "displayName", "role",
                "status", "cloudLeagueId", "clubCloudId", "createdAt", "updatedAt",
            ),
            payload.keys,
        )
        assertTrue(payload["firebaseUid"] is String)
        assertTrue(payload["localUserId"] is Long)
        assertEquals("PRESIDENT", payload["role"])
        assertEquals("ACTIVE", payload["status"])
        assertNull(payload["cloudLeagueId"])
        assertNull(payload["clubCloudId"])
    }

    @Test
    fun `payload round trip preserves a complete profile`() {
        val original = profile()

        val decoded = CloudUserProfileDocument.decode("uid-1", CloudUserProfileDocument.payload(original))

        assertEquals(original, decoded)
    }

    @Test(expected = OnlineProfileIncompleteException::class)
    fun `existing incomplete profile is rejected with missing field detail`() {
        val incomplete = CloudUserProfileDocument.payload(profile()).minus("username")

        CloudUserProfileDocument.decode("uid-1", incomplete)
    }

    @Test(expected = OnlineProfileCorruptException::class)
    fun `profile with invalid role type is rejected`() {
        val invalid = CloudUserProfileDocument.payload(profile()).toMutableMap().apply {
            this["role"] = 1L
        }

        CloudUserProfileDocument.decode("uid-1", invalid)
    }

    private fun profile() = CloudUserProfile(
        firebaseUid = "uid-1",
        localUserId = 9L,
        username = "presidente_uid1",
        displayName = "Presidente",
        role = UserRole.PRESIDENT,
        status = "ACTIVE",
        cloudLeagueId = null,
        clubCloudId = null,
        createdAt = 100L,
        updatedAt = 200L,
    )
}
