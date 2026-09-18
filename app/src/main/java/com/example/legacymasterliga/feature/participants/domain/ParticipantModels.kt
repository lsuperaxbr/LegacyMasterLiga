package com.example.legacymasterliga.feature.participants.domain

data class ParticipantClub(
    val id: Long,
    val name: String,
    val crestUri: String?,
    val selected: Boolean,
)

data class SeasonOption(
    val id: Long,
    val competitionId: Long,
    val competitionName: String,
    val seasonName: String,
)
