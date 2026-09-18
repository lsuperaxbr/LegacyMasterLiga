package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prize_history",
    foreignKeys = [
        ForeignKey(entity = LeagueEntity::class, parentColumns = ["id"], childColumns = ["leagueId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = CompetitionEntity::class, parentColumns = ["id"], childColumns = ["competitionId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = SeasonEntity::class, parentColumns = ["id"], childColumns = ["seasonId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = ClubEntity::class, parentColumns = ["id"], childColumns = ["clubId"], onDelete = ForeignKey.RESTRICT, onUpdate = ForeignKey.CASCADE),
    ],
    indices = [
        Index("leagueId"), Index("competitionId"), Index("seasonId"), Index("clubId"), Index("awardedAt"),
        Index(value = ["seasonId", "clubId", "prizeType"], unique = true),
    ],
)
data class PrizeHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leagueId: Long,
    val competitionId: Long,
    val seasonId: Long,
    val clubId: Long,
    val prizeType: String,
    val amountCr: Long,
    val description: String,
    val awardedAt: Long = System.currentTimeMillis(),
)
