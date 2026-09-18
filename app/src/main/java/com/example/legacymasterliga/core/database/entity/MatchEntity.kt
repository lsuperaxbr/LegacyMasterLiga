package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.legacymasterliga.core.model.MatchStatus

@Entity(
    tableName = "matches",
    foreignKeys = [
        ForeignKey(
            entity = SeasonEntity::class,
            parentColumns = ["id"],
            childColumns = ["seasonId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = RoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["homeClubId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["awayClubId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["seasonId"]),
        Index(value = ["roundId"]),
        Index(value = ["homeClubId"]),
        Index(value = ["awayClubId"]),
        Index(value = ["seasonId", "pairingKey", "leg"], unique = true),
        Index(value = ["status"]),
    ],
)
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val seasonId: Long,
    val roundId: Long,
    val homeClubId: Long,
    val awayClubId: Long,
    val pairingKey: String,
    val leg: Int = 1,
    val stage: String = "REGULAR",
    val groupIndex: Int? = null,
    val bracketPosition: Int? = null,
    val penaltiesHome: Int? = null,
    val penaltiesAway: Int? = null,
    val winnerClubId: Long? = null,
    val advanceReason: String? = null,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val homeYellowCards: Int = 0,
    val homeRedCards: Int = 0,
    val awayYellowCards: Int = 0,
    val awayRedCards: Int = 0,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val scheduledAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
