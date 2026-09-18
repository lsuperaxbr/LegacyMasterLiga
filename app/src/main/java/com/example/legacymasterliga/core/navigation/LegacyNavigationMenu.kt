package com.example.legacymasterliga.core.navigation

import com.example.legacymasterliga.core.model.UserRole

data class LegacyNavigationContext(
    val leagueId: Long = 0L,
    val competitionId: Long = 0L,
    val seasonId: Long = 0L,
) {
    val isComplete: Boolean
        get() = leagueId > 0L && competitionId > 0L && seasonId > 0L
}

enum class LegacyMenuIcon {
    LEAGUE,
    CUP,
    MARKET,
    CLUBS,
    FINANCE,
    CENTRAL,
    HISTORY,
    MORE,
    COMPETITION,
    ROUNDS,
    STANDINGS,
    STATISTICS,
    CLOSURE,
    NEWS,
    NOTIFICATIONS,
    USERS,
    SETTINGS,
    BACKUP,
    AUDIT,
    PERFORMANCE,
    EXPORT,
    BETA,
    ARENA,
    SPORTS_SOCCER,
}

data class LegacyMenuItem(
    val title: String,
    val subtitle: String,
    val icon: LegacyMenuIcon,
    val route: String,
    val highlighted: Boolean = false,
)

data class LegacyMenuSection(
    val title: String,
    val items: List<LegacyMenuItem>,
)

object LegacyNavigationMenu {
    fun dashboard(role: UserRole?): List<LegacyMenuItem> {
        if (role == null) return emptyList()

        return buildList {
            add(item("Liga", "Sua central única de competição", LegacyMenuIcon.LEAGUE, LegacyDestination.LeagueHub.createRoute()))
            add(item("Arena", "Duelos de aposta direta", LegacyMenuIcon.ARENA, LegacyDestination.Arena.route))
            add(item("Leilão", "Lances em jogadores do Banco", LegacyMenuIcon.MARKET, LegacyDestination.AuctionList.route))
            if (role != UserRole.VISITOR) {
                add(item("Mercado", "Negociações e transferências", LegacyMenuIcon.MARKET, LegacyDestination.Market.route))
            }
            if (role != UserRole.VISITOR) {
                add(item("Meu Perfil", "Estatísticas e carreira", LegacyMenuIcon.STATISTICS, LegacyDestination.PresidentProfile.route))
            }
            add(item("Clubes", "Elencos e perfis", LegacyMenuIcon.CLUBS, LegacyDestination.Clubs.route))
            if (role != UserRole.VISITOR) {
                add(item("Financeiro", "Saldos e extratos em CR", LegacyMenuIcon.FINANCE, LegacyDestination.Finance.route))
            }
            add(item("Central", "Notícias e avisos", LegacyMenuIcon.CENTRAL, LegacyDestination.CentralHub.route))
            add(item("História", "Temporadas, campeões e recordes", LegacyMenuIcon.HISTORY, LegacyDestination.HistoryHub.route))
            if (role == UserRole.ADMINISTRATOR || role == UserRole.PRESIDENT) {
                add(item("Mais", "Gestão e ferramentas do sistema", LegacyMenuIcon.MORE, LegacyDestination.MoreHub.route))
            }
        }
    }

    fun league(
        role: UserRole?,
        context: LegacyNavigationContext,
        hasActiveSeason: Boolean
    ): List<LegacyMenuSection> {
        if (role == null) return emptyList()

        val items = buildList {
            if (hasActiveSeason) {
                add(item("Continuar Temporada", "Acessar rodada atual", LegacyMenuIcon.LEAGUE, scheduleRoute(context, openBracket = false), highlighted = true))
            } else if (role == UserRole.ADMINISTRATOR) {
                add(item("Nova Temporada", "Criar e configurar a temporada", LegacyMenuIcon.COMPETITION, LegacyDestination.Competitions.route, highlighted = true))
            }

            add(item("Rodadas", "Calendário e lançamento de resultados", LegacyMenuIcon.ROUNDS, scheduleRoute(context, openBracket = false)))
            add(item("Classificação", "Tabela oficial da Liga", LegacyMenuIcon.STANDINGS, LegacyDestination.Standings.route))
            add(item("Artilharia", "Ranking de goleadores", LegacyMenuIcon.SPORTS_SOCCER, LegacyDestination.TopScorers.route))
            add(item("Estatísticas", "Números da competição", LegacyMenuIcon.STATISTICS, LegacyDestination.Statistics.route))
            
            if (role == UserRole.ADMINISTRATOR) {
                add(item("Premiação", "Distribuir prêmios da temporada", LegacyMenuIcon.CLOSURE, LegacyDestination.SeasonClosure.route))
                add(item("Encerrar Temporada", "Finalizar a competição atual", LegacyMenuIcon.CLOSURE, LegacyDestination.SeasonClosure.route))
                add(item("Configurações da Liga", "Regras e opções da competição", LegacyMenuIcon.SETTINGS, LegacyDestination.LeagueSettings.route))
            }
            
            if (hasActiveSeason && role == UserRole.ADMINISTRATOR) {
                add(item("Nova Temporada", "Criar próxima temporada", LegacyMenuIcon.COMPETITION, LegacyDestination.Competitions.route))
            }
        }
        return listOf(LegacyMenuSection("Liga", items))
    }

    fun cup(role: UserRole?, context: LegacyNavigationContext): List<LegacyMenuSection> {
        @Suppress("UNUSED_VARIABLE") val ignored = role to context
        return emptyList()
    }

    fun central(): List<LegacyMenuSection> = listOf(
        LegacyMenuSection(
            title = "Central",
            items = listOf(
                item("Notícias", "Acontecimentos da Master Liga", LegacyMenuIcon.NEWS, LegacyDestination.News.route),
                item("Avisos", "Notificações importantes", LegacyMenuIcon.NOTIFICATIONS, LegacyDestination.Notifications.route),
            ),
        ),
    )

    fun history(): List<LegacyMenuSection> = listOf(
        LegacyMenuSection(
            title = "História",
            items = listOf(
                item("Temporadas e campeões", "Memória oficial das competições", LegacyMenuIcon.HISTORY, LegacyDestination.History.route),
                item("Recordes", "Hall da Fama da Master Liga", LegacyMenuIcon.CUP, LegacyDestination.HallOfFame.route),
            ),
        ),
    )

    fun more(role: UserRole?): List<LegacyMenuSection> {
        if (role == null || role == UserRole.VISITOR) return emptyList()

        return buildList {
            if (role == UserRole.ADMINISTRATOR) {
                add(
                    LegacyMenuSection(
                        title = "Sistema",
                        items = listOf(
                            item("Backup", "Cópia e restauração dos dados", LegacyMenuIcon.BACKUP, LegacyDestination.Backup.route),
                            item("Auditoria", "Logs e rastreabilidade", LegacyMenuIcon.AUDIT, LegacyDestination.Audit.route),
                            item("Desempenho", "Métricas, cache e erros", LegacyMenuIcon.PERFORMANCE, LegacyDestination.Performance.route),
                            item("Exportação", "Relatórios PDF e CSV", LegacyMenuIcon.EXPORT, LegacyDestination.Reports.route),
                            item("Guia Beta", "Roteiro dos testes", LegacyMenuIcon.BETA, LegacyDestination.BetaTestGuide.route),
                        ),
                    )
                )
            }

            add(
                LegacyMenuSection(
                    title = "Gestão",
                    items = buildList {
                        if (role == UserRole.ADMINISTRATOR) {
                            add(item("Usuários", "Perfis e permissões", LegacyMenuIcon.USERS, LegacyDestination.Users.route))
                        }
                        add(item("Configurações", "Ajustes do aplicativo", LegacyMenuIcon.SETTINGS, LegacyDestination.Settings.route))
                    },
                )
            )
        }
    }

    private fun scheduleRoute(context: LegacyNavigationContext, openBracket: Boolean): String =
        LegacyDestination.Schedule.createRoute(
            leagueId = context.leagueId,
            competitionId = context.competitionId,
            seasonId = context.seasonId,
            openBracket = openBracket,
        )

    private fun item(
        title: String,
        subtitle: String,
        icon: LegacyMenuIcon,
        route: String,
        highlighted: Boolean = false,
    ) = LegacyMenuItem(title, subtitle, icon, route, highlighted)
}
