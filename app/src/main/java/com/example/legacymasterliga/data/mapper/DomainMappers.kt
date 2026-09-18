package com.example.legacymasterliga.data.mapper

import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import com.example.legacymasterliga.core.database.entity.PlayerEntity
import com.example.legacymasterliga.core.database.model.PlayerWithClub
import com.example.legacymasterliga.core.database.entity.UserEntity
import com.example.legacymasterliga.domain.model.Club
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.model.Player
import com.example.legacymasterliga.domain.model.User

internal fun UserEntity.toDomain(): User = User(
    id = id,
    username = username,
    displayName = displayName,
    role = role,
    status = status,
)

internal fun LeagueEntity.toDomain(): League = League(
    id = id,
    name = name,
    currencyCode = currencyCode,
    status = status,
    cloudLeagueId = cloudLeagueId,
    isOnline = isOnline,
)

internal fun ClubEntity.toDomain(): Club = Club(
    id = id,
    leagueId = leagueId,
    name = name,
    crestUri = crestUri,
    presidentUserId = presidentUserId,
    isBank = isBank,
    isActive = isActive,
)

internal fun PlayerEntity.toDomain(): Player = Player(
    id = id,
    leagueId = leagueId,
    clubId = clubId,
    name = name,
    position = position,
    marketStatus = marketStatus,
    askingPriceCr = askingPriceCr,
    skillImageUri = skillImageUri,
    notes = notes,
    externalPlayerId = externalPlayerId,
    attributesRaw = attributesRaw,
    overall = overall
)

internal fun PlayerWithClub.toDomain(): Player = player.toDomain().copy(clubName = clubName)
