package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["firebaseUid"], unique = true),
        Index(value = ["role"]),
        Index(value = ["status"]),
    ],
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val displayName: String,
    val passwordHash: String,
    val passwordSalt: String,
    val role: UserRole,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val firebaseUid: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
