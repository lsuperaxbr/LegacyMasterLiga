package com.example.legacymasterliga.domain.model

import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.UserRole

data class User(
    val id: Long,
    val username: String,
    val displayName: String,
    val role: UserRole,
    val status: AccountStatus,
    val isOnlineSession: Boolean = false,
)
