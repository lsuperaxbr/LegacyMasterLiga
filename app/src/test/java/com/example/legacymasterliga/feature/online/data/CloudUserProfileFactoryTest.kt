package com.example.legacymasterliga.feature.online.data

import com.example.legacymasterliga.core.database.entity.UserEntity
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudUserProfileFactoryTest {

    @Test
    fun `orphan Firebase account receives active President profile`() {
        val profile = CloudUserProfileFactory.create(
            uid = "12345678abcdef",
            email = "Luiz.Presidente@liga.com",
            firebaseDisplayName = null,
            localUser = null,
            now = 1_000L,
        )

        assertEquals("12345678abcdef", profile.firebaseUid)
        assertEquals(0L, profile.localUserId)
        assertEquals(UserRole.PRESIDENT, profile.role)
        assertEquals("ACTIVE", profile.status)
        assertEquals("Luiz.Presidente", profile.displayName)
        assertEquals("luiz_presidente_12345678", profile.username)
        assertEquals(1_000L, profile.createdAt)
        assertEquals(1_000L, profile.updatedAt)
    }

    @Test
    fun `existing local President metadata is preserved in new cloud profile`() {
        val localUser = UserEntity(
            id = 42L,
            username = "luiz",
            displayName = "Luiz",
            passwordHash = "hash",
            passwordSalt = "salt",
            role = UserRole.PRESIDENT,
            status = AccountStatus.ACTIVE,
        )

        val profile = CloudUserProfileFactory.create(
            uid = "uid-local",
            email = "outra@liga.com",
            firebaseDisplayName = "Nome Firebase",
            localUser = localUser,
            now = 2_000L,
        )

        assertEquals(42L, profile.localUserId)
        assertEquals("luiz", profile.username)
        assertEquals("Nome Firebase", profile.displayName)
        assertEquals(UserRole.PRESIDENT, profile.role)
    }

    @Test
    fun `client bootstrap never copies local administrator privilege to Firestore`() {
        val localAdmin = UserEntity(
            id = 7L,
            username = "admin",
            displayName = "Administrador",
            passwordHash = "hash",
            passwordSalt = "salt",
            role = UserRole.ADMINISTRATOR,
            status = AccountStatus.ACTIVE,
        )

        val profile = CloudUserProfileFactory.create(
            uid = "safe-uid",
            email = "admin@liga.com",
            firebaseDisplayName = null,
            localUser = localAdmin,
            now = 3_000L,
        )

        assertEquals(UserRole.PRESIDENT, profile.role)
        assertEquals("ACTIVE", profile.status)
    }

    @Test
    fun `custom identity is respected over email or local user`() {
        val localUser = UserEntity(
            id = 42L,
            username = "local_nick",
            displayName = "Local Name",
            passwordHash = "h",
            passwordSalt = "s",
            role = UserRole.PRESIDENT,
            status = AccountStatus.ACTIVE,
        )

        val profile = CloudUserProfileFactory.create(
            uid = "uid-1",
            email = "email@liga.com",
            firebaseDisplayName = "Firebase Name",
            localUser = localUser,
            customDisplayName = "Custom Name",
            customUsername = "custom_nick",
            now = 5_000L,
        )

        assertEquals("Custom Name", profile.displayName)
        assertEquals("custom_nick", profile.username)
        assertEquals(42L, profile.localUserId)
    }
}
