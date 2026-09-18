package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = LeagueEntity::class,
            parentColumns = ["id"],
            childColumns = ["leagueId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("leagueId"),
        Index("category"),
        Index("audience"),
        Index("createdAt"),
        Index(value = ["sourceKey"], unique = true),
    ],
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leagueId: Long? = null,
    val category: String,
    val title: String,
    val message: String,
    val audience: String = "ALL",
    val priority: String = "NORMAL",
    val sourceKey: String,
    val destinationRoute: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "notification_reads",
    primaryKeys = ["notificationId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = NotificationEntity::class,
            parentColumns = ["id"],
            childColumns = ["notificationId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("userId"), Index("readAt")],
)
data class NotificationReadEntity(
    val notificationId: Long,
    val userId: Long,
    val readAt: Long = System.currentTimeMillis(),
)
