package com.example.legacymasterliga.core.database.converter

import androidx.room.TypeConverter
import com.example.legacymasterliga.core.model.AccountStatus
import com.example.legacymasterliga.core.model.CompetitionFormat
import com.example.legacymasterliga.core.model.CompetitionStatus
import com.example.legacymasterliga.core.model.CompetitionType
import com.example.legacymasterliga.core.model.LeagueStatus
import com.example.legacymasterliga.core.model.MatchStatus
import com.example.legacymasterliga.core.model.RoundStatus
import com.example.legacymasterliga.core.model.SeasonStatus
import com.example.legacymasterliga.core.model.UserRole
import com.example.legacymasterliga.core.model.ThemePreference
import com.example.legacymasterliga.core.model.DensityPreference
import com.example.legacymasterliga.core.model.MarketStatus

class LegacyTypeConverters {
    @TypeConverter
    fun userRoleToString(value: UserRole?): String? = value?.name

    @TypeConverter
    fun stringToUserRole(value: String?): UserRole? = value?.let(UserRole::valueOf)

    @TypeConverter
    fun accountStatusToString(value: AccountStatus?): String? = value?.name

    @TypeConverter
    fun stringToAccountStatus(value: String?): AccountStatus? =
        value?.let(AccountStatus::valueOf)

    @TypeConverter
    fun leagueStatusToString(value: LeagueStatus?): String? = value?.name

    @TypeConverter
    fun stringToLeagueStatus(value: String?): LeagueStatus? =
        value?.let(LeagueStatus::valueOf)

    @TypeConverter
    fun competitionTypeToString(value: CompetitionType?): String? = value?.name

    @TypeConverter
    fun stringToCompetitionType(value: String?): CompetitionType? =
        value?.let(CompetitionType::valueOf)

    @TypeConverter
    fun competitionFormatToString(value: CompetitionFormat?): String? = value?.name

    @TypeConverter
    fun stringToCompetitionFormat(value: String?): CompetitionFormat? =
        value?.let(CompetitionFormat::valueOf)

    @TypeConverter
    fun competitionStatusToString(value: CompetitionStatus?): String? = value?.name

    @TypeConverter
    fun stringToCompetitionStatus(value: String?): CompetitionStatus? =
        value?.let(CompetitionStatus::valueOf)

    @TypeConverter
    fun seasonStatusToString(value: SeasonStatus?): String? = value?.name

    @TypeConverter
    fun stringToSeasonStatus(value: String?): SeasonStatus? =
        value?.let(SeasonStatus::valueOf)

    @TypeConverter
    fun roundStatusToString(value: RoundStatus?): String? = value?.name

    @TypeConverter
    fun stringToRoundStatus(value: String?): RoundStatus? = value?.let(RoundStatus::valueOf)

    @TypeConverter
    fun matchStatusToString(value: MatchStatus?): String? = value?.name

    @TypeConverter
    fun stringToMatchStatus(value: String?): MatchStatus? = value?.let(MatchStatus::valueOf)

    @TypeConverter
    fun themePreferenceToString(value: ThemePreference?): String? = value?.name

    @TypeConverter
    fun stringToThemePreference(value: String?): ThemePreference? =
        value?.let(ThemePreference::valueOf)

    @TypeConverter
    fun densityPreferenceToString(value: DensityPreference?): String? = value?.name

    @TypeConverter
    fun stringToDensityPreference(value: String?): DensityPreference? =
        value?.let(DensityPreference::valueOf)

    @TypeConverter
    fun marketStatusToString(value: MarketStatus?): String? = value?.name

    @TypeConverter
    fun stringToMarketStatus(value: String?): MarketStatus? =
        value?.let(MarketStatus::valueOf)
}

