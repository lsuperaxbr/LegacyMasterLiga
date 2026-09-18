package com.example.legacymasterliga.feature.schedule.domain

import com.example.legacymasterliga.core.model.CompetitionFormat

object ScheduleGenerator {
    fun generate(
        clubIds: List<Long>,
        format: CompetitionFormat,
        groupCount: Int? = null,
        qualifiedPerGroup: Int? = null,
        knockoutLegs: Int = 1,
    ): List<GeneratedFixture> {
        val distinctIds = clubIds.distinct()
        require(distinctIds.size >= 2) { "Inscreva pelo menos dois clubes antes de gerar os jogos." }

        return when (format) {
            CompetitionFormat.SINGLE_ROUND -> roundRobin(distinctIds, includeReturnLeg = false)
            CompetitionFormat.HOME_AND_AWAY -> roundRobin(distinctIds, includeReturnLeg = true)
            CompetitionFormat.SINGLE_MATCH -> CupEngine.openingRound(distinctIds, legs = 1)
            CompetitionFormat.KNOCKOUT -> CupEngine.openingRound(distinctIds, legs = knockoutLegs)
            CompetitionFormat.GROUPS_AND_KNOCKOUT -> groupsPhase(distinctIds, groupCount ?: 1)
        }
    }

    private fun groupsPhase(clubIds: List<Long>, count: Int): List<GeneratedFixture> {
        val groups = List(count) { mutableListOf<Long>() }
        clubIds.forEachIndexed { index, id -> groups[index % count].add(id) }

        val allFixtures = mutableListOf<GeneratedFixture>()
        groups.forEachIndexed { groupIndex, members ->
            if (members.size >= 2) {
                val groupFixtures = roundRobin(members, includeReturnLeg = true)
                allFixtures += groupFixtures.map { it.copy(groupIndex = groupIndex, stage = "GROUPS") }
            }
        }
        return allFixtures
    }

    private fun roundRobin(clubIds: List<Long>, includeReturnLeg: Boolean): List<GeneratedFixture> {
        val rotation = clubIds.map<Long, Long?> { it }.toMutableList()
        if (rotation.size % 2 != 0) rotation += null

        val teamCount = rotation.size
        val firstLeg = mutableListOf<GeneratedFixture>()

        repeat(teamCount - 1) { roundIndex ->
            for (pairIndex in 0 until teamCount / 2) {
                val first = rotation[pairIndex]
                val second = rotation[teamCount - 1 - pairIndex]
                if (first != null && second != null) {
                    val invert = (roundIndex + pairIndex) % 2 != 0
                    firstLeg += GeneratedFixture(
                        roundNumber = roundIndex + 1,
                        homeClubId = if (invert) second else first,
                        awayClubId = if (invert) first else second,
                        leg = 1,
                    )
                }
            }
            val last = rotation.removeAt(rotation.lastIndex)
            rotation.add(1, last)
        }

        if (!includeReturnLeg) return firstLeg
        val roundOffset = teamCount - 1
        return firstLeg + firstLeg.map { fixture ->
            fixture.copy(
                roundNumber = fixture.roundNumber + roundOffset,
                homeClubId = fixture.awayClubId,
                awayClubId = fixture.homeClubId,
                leg = 2,
            )
        }
    }
}
