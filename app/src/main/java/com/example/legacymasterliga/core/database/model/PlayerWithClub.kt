package com.example.legacymasterliga.core.database.model

import androidx.room.Embedded
import com.example.legacymasterliga.core.database.entity.PlayerEntity

data class PlayerWithClub(
    @Embedded val player: PlayerEntity,
    val clubName: String
)
