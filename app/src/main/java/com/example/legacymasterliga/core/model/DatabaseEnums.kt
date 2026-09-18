package com.example.legacymasterliga.core.model

enum class UserRole {
    ADMINISTRATOR,
    PRESIDENT,
    VISITOR,
}

enum class AccountStatus {
    ACTIVE,
    DISABLED,
    BLOCKED,
}

enum class LeagueStatus {
    ACTIVE,
    ARCHIVED,
}

enum class CompetitionType {
    LEAGUE,
    CUP,
    SUPER_CUP,
}

enum class CompetitionFormat {
    SINGLE_ROUND,
    HOME_AND_AWAY,
    SINGLE_MATCH,
    KNOCKOUT,
    GROUPS_AND_KNOCKOUT,
}

enum class CompetitionStatus {
    DRAFT,
    ACTIVE,
    FINISHED,
    ARCHIVED,
}

enum class SeasonStatus {
    DRAFT,
    ACTIVE,
    FINISHED,
    ARCHIVED,
}


enum class RoundStatus {
    SCHEDULED,
    IN_PROGRESS,
    FINISHED,
}

enum class MatchStatus {
    SCHEDULED,
    FINISHED,
    CANCELLED,
}

enum class ThemePreference {
    SYSTEM,
    DARK,
    LIGHT,
}

enum class DensityPreference {
    COMFORTABLE,
    COMPACT,
}

enum class TieBreakCriterion {
    POINTS,
    WINS,
    GOAL_DIFFERENCE,
    GOALS_FOR,
    HEAD_TO_HEAD,
}
