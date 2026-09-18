package com.example.legacymasterliga.feature.news.domain

import javax.inject.Inject

data class GeneratedNews(val title: String, val body: String, val category: String, val eventType: String)

class NewsTemplateFactory @Inject constructor() {
    fun render(event: NewsEvent): GeneratedNews = when (event) {
        is TransferCompletedEvent -> transfer(event)
        is SwapCompletedEvent -> swap(event)
        is MatchFinishedEvent -> match(event)
        is LeadershipChangedEvent -> leadership(event)
        is ChampionCrownedEvent -> champion(event)
    }

    private fun transfer(event: TransferCompletedEvent): GeneratedNews {
        val freeAgent = event.originClubName.contains("Banco", ignoreCase = true)
        val release = event.destinationClubName.contains("Banco", ignoreCase = true)

        return when {
            freeAgent && event.valueCr == 0L -> {
                GeneratedNews(
                    "${event.destinationClubName} anuncia reforço livre",
                    "${event.destinationClubName} anuncia a contratação de ${event.playerName}, que estava livre no mercado.",
                    "CONTRATAÇÃO", "TRANSFER"
                )
            }
            freeAgent -> {
                GeneratedNews(
                    "${event.destinationClubName} contrata do Banco",
                    "${event.destinationClubName} contrata ${event.playerName} do Banco da Liga por ${event.valueCr} CR.",
                    "MERCADO", "TRANSFER"
                )
            }
            release -> {
                GeneratedNews(
                    "${event.playerName} livre no mercado",
                    "${event.originClubName} libera ${event.playerName} para o Banco da Liga e paga multa de 5 CR.",
                    "LIBERAÇÃO", "TRANSFER"
                )
            }
            event.valueCr == 0L -> {
                GeneratedNews(
                    "Transferência gratuita: ${event.playerName}",
                    "${event.playerName} deixa o ${event.originClubName} e reforça o ${event.destinationClubName} sem custos.",
                    "MERCADO", "TRANSFER"
                )
            }
            else -> {
                val titles = listOf(
                    "${event.destinationClubName} anuncia ${event.playerName}",
                    "Mercado agitado: ${event.playerName} muda de clube",
                    "${event.playerName} é o novo reforço do ${event.destinationClubName}",
                    "Negócio fechado por ${event.valueCr} CR",
                )
                val bodies = listOf(
                    "${event.originClubName} e ${event.destinationClubName} concluíram a transferência de ${event.playerName} por ${event.valueCr} CR.",
                    "O mercado confirmou a ida de ${event.playerName} do ${event.originClubName} para o ${event.destinationClubName}. A operação movimentou ${event.valueCr} CR.",
                    "Após acordo entre os clubes, ${event.playerName} deixa o ${event.originClubName} e passa a defender o ${event.destinationClubName} por ${event.valueCr} CR.",
                )
                GeneratedNews(titles.pick(event.dedupKey), bodies.pick(event.dedupKey + ":BODY"), "TRANSFERÊNCIA", "TRANSFER")
            }
        }
    }

    private fun swap(event: SwapCompletedEvent): GeneratedNews {
        val baseBody = "${event.clubAName} e ${event.clubBName} acertam troca envolvendo ${event.playerA} e ${event.playerB}."
        val compensationText = if (event.compensationCr > 0) {
            " O ${event.payerClubName} pagou ${event.compensationCr} CR como compensação financeira."
        } else ""
        
        return GeneratedNews(
            "Troca confirmada: ${event.playerA} x ${event.playerB}",
            baseBody + compensationText,
            "TROCA", "TRANSFER"
        )
    }

    private fun match(event: MatchFinishedEvent): GeneratedNews {
        val margin = kotlin.math.abs(event.homeScore - event.awayScore)
        val score = "${event.homeScore} a ${event.awayScore}"
        if (margin >= GOLEADA_MARGIN) {
            val winner = if (event.homeScore > event.awayScore) event.homeClubName else event.awayClubName
            val loser = if (event.homeScore > event.awayScore) event.awayClubName else event.homeClubName
            val titles = listOf(
                "Goleada! $winner domina $loser",
                "Show de gols do $winner",
                "$winner atropela $loser",
                "Placar elástico marca a rodada",
            )
            val bodies = listOf(
                "$winner venceu $loser por $score em ${event.competitionName}, ${event.seasonName}.",
                "A torcida viu uma atuação dominante: $winner fez $score sobre $loser pela ${event.competitionName}.",
                "Com diferença de $margin gols, $winner confirmou uma grande vitória diante de $loser: $score.",
            )
            return GeneratedNews(titles.pick(event.dedupKey), bodies.pick(event.dedupKey + ":BODY"), "GOLEADA", "MATCH")
        }
        if (event.homeScore == event.awayScore) {
            val titles = listOf("Tudo igual entre ${event.homeClubName} e ${event.awayClubName}", "Empate movimenta a ${event.competitionName}", "Pontos divididos em duelo equilibrado")
            val bodies = listOf(
                "${event.homeClubName} e ${event.awayClubName} empataram por $score em ${event.seasonName}.",
                "Sem vencedor: ${event.homeClubName} e ${event.awayClubName} ficaram no $score pela ${event.competitionName}.",
            )
            return GeneratedNews(titles.pick(event.dedupKey), bodies.pick(event.dedupKey + ":BODY"), "RESULTADO", "MATCH")
        }
        val winner = if (event.homeScore > event.awayScore) event.homeClubName else event.awayClubName
        val loser = if (event.homeScore > event.awayScore) event.awayClubName else event.homeClubName
        val titles = listOf("$winner vence $loser", "$winner soma três pontos", "Vitória do $winner agita a competição", "$winner leva a melhor no confronto")
        val bodies = listOf(
            "$winner derrotou $loser por $score em ${event.competitionName}, ${event.seasonName}.",
            "O confronto terminou $score para o $winner diante do $loser pela ${event.competitionName}.",
            "$winner confirmou a vitória sobre $loser pelo placar de $score.",
        )
        return GeneratedNews(titles.pick(event.dedupKey), bodies.pick(event.dedupKey + ":BODY"), "RESULTADO", "MATCH")
    }

    private fun leadership(event: LeadershipChangedEvent): GeneratedNews {
        val titles = listOf("${event.newLeaderName} assume a liderança", "Tem novo líder na ${event.competitionName}", "${event.newLeaderName} chega ao topo da tabela")
        val bodies = listOf(
            "${event.newLeaderName} ultrapassou ${event.previousLeaderName} e agora lidera ${event.competitionName}, ${event.seasonName}.",
            "A ponta mudou de mãos: ${event.newLeaderName} tomou a primeira posição que era de ${event.previousLeaderName}.",
            "Após a atualização da classificação, ${event.newLeaderName} aparece em primeiro, à frente do antigo líder ${event.previousLeaderName}.",
        )
        return GeneratedNews(titles.pick(event.dedupKey), bodies.pick(event.dedupKey + ":BODY"), "LIDERANÇA", "LEADERSHIP")
    }

    private fun champion(event: ChampionCrownedEvent): GeneratedNews {
        val titles = listOf("${event.championClubName} é campeão!", "Título confirmado para ${event.championClubName}", "${event.championClubName} conquista a ${event.competitionName}", "Festa do campeão: ${event.championClubName}")
        val bodies = listOf(
            "${event.championClubName} encerrou ${event.seasonName} no topo com ${event.points} pontos e conquistou ${event.competitionName}.",
            "A temporada terminou com título do ${event.championClubName}, campeão de ${event.competitionName} com ${event.points} pontos.",
            "Após a última partida, ${event.championClubName} confirmou a conquista de ${event.competitionName}, ${event.seasonName}.",
        )
        return GeneratedNews(titles.pick(event.dedupKey), bodies.pick(event.dedupKey + ":BODY"), "CONQUISTA", "CHAMPION")
    }

    private fun List<String>.pick(seed: String): String = this[Math.floorMod(seed.hashCode(), size)]

    private companion object { const val GOLEADA_MARGIN = 3 }
}
