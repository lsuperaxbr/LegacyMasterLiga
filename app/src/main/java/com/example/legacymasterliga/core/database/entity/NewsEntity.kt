package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "news",
    foreignKeys = [
        ForeignKey(entity = LeagueEntity::class, parentColumns = ["id"], childColumns = ["leagueId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CompetitionEntity::class, parentColumns = ["id"], childColumns = ["competitionId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = SeasonEntity::class, parentColumns = ["id"], childColumns = ["seasonId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("leagueId"), Index("competitionId"), Index("seasonId"), Index("publishedAt"), Index(value = ["dedupKey"], unique = true)],
)
data class NewsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leagueId: Long,
    val competitionId: Long? = null,
    val seasonId: Long? = null,
    val title: String,
    val body: String,
    val category: String,
    val eventType: String,
    val sourceId: Long? = null,
    val dedupKey: String,
    val imageUri: String? = null,
    val authorUserId: Long? = null,
    val publishedAt: Long = System.currentTimeMillis(),
)
