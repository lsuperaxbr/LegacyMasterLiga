package com.example.legacymasterliga.feature.schedule.domain

/** Pure knockout rules. Persistence and UI must not infer cup phases independently. */
object CupEngine {
    const val STAGE = "KNOCKOUT"
    const val PRELIMINARY = "Fase Preliminar"
    const val ROUND_OF_32 = "Dezesseis-avos de Final"
    const val ROUND_OF_16 = "Oitavas de Final"
    const val QUARTER_FINALS = "Quartas de Final"
    const val SEMI_FINALS = "Semifinais"
    const val FINAL = "Final"

    data class LegResult(
        val leg: Int,
        val homeClubId: Long,
        val awayClubId: Long,
        val homeScore: Int?,
        val awayScore: Int?,
        val winnerClubId: Long?,
    )

    fun openingRound(clubIds: List<Long>, legs: Int): List<GeneratedFixture> {
        val entrants = clubIds.distinct()
        require(entrants.size >= 2) { "Inscreva pelo menos dois clubes antes de gerar os jogos." }
        require(legs in 1..2) { "A Copa deve usar jogo único ou ida e volta." }

        val lowerPowerOfTwo = Integer.highestOneBit(entrants.size)
        val preliminaryPairCount = entrants.size - lowerPowerOfTwo
        val playingClubCount = if (preliminaryPairCount == 0) entrants.size else preliminaryPairCount * 2
        val playingClubs = entrants.takeLast(playingClubCount)
        val phaseLabel = if (preliminaryPairCount > 0) PRELIMINARY else phaseLabel(entrants.size)

        return playingClubs.chunked(2).flatMapIndexed { bracketPosition, pair ->
            check(pair.size == 2) { "O chaveamento da Copa produziu um confronto incompleto." }
            buildList {
                add(
                    GeneratedFixture(
                        roundNumber = 1,
                        roundName = roundName(phaseLabel, leg = 1, legs = legs),
                        stageLabel = phaseLabel,
                        homeClubId = pair[0],
                        awayClubId = pair[1],
                        leg = 1,
                        stage = STAGE,
                        bracketPosition = bracketPosition,
                    ),
                )
                if (legs == 2) {
                    add(
                        GeneratedFixture(
                            roundNumber = 2,
                            roundName = roundName(phaseLabel, leg = 2, legs = legs),
                            stageLabel = phaseLabel,
                            homeClubId = pair[1],
                            awayClubId = pair[0],
                            leg = 2,
                            stage = STAGE,
                            bracketPosition = bracketPosition,
                        ),
                    )
                }
            }
        }
    }

    fun phaseLabel(entrantCount: Int): String = when (entrantCount) {
        2 -> FINAL
        4 -> SEMI_FINALS
        8 -> QUARTER_FINALS
        16 -> ROUND_OF_16
        32 -> ROUND_OF_32
        else -> "Fase de $entrantCount clubes"
    }

    fun roundName(label: String, leg: Int, legs: Int): String = when {
        legs == 1 -> label
        leg == 1 -> "$label - Ida"
        else -> "$label - Volta"
    }

    fun canonicalPhase(stageLabel: String?, roundName: String): String =
        stageLabel?.takeIf { it.isNotBlank() }
            ?: roundName.removeSuffix(" - Ida").removeSuffix(" - Volta")

    fun isFinal(stageLabel: String?, roundName: String): Boolean =
        canonicalPhase(stageLabel, roundName) == FINAL

    fun winner(results: List<LegResult>): Long? {
        if (results.isEmpty()) return null
        if (results.size == 1) return results.single().winnerClubId

        val firstLeg = results.firstOrNull { it.leg == 1 } ?: return null
        val secondLeg = results.firstOrNull { it.leg == 2 } ?: return null
        val firstClubTotal = (firstLeg.homeScore ?: 0) + (secondLeg.awayScore ?: 0)
        val secondClubTotal = (firstLeg.awayScore ?: 0) + (secondLeg.homeScore ?: 0)

        return when {
            firstClubTotal > secondClubTotal -> firstLeg.homeClubId
            secondClubTotal > firstClubTotal -> firstLeg.awayClubId
            else -> secondLeg.winnerClubId ?: firstLeg.winnerClubId
        }
    }
}
