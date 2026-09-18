package com.example.legacymasterliga.core.database.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "financial_transactions",
    foreignKeys = [
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["clubId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("clubId"), Index("createdAt"), Index("transferId")],
)
data class FinancialTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clubId: Long,
    val amountCr: Long,
    val description: String,
    @ColumnInfo(defaultValue = "'ADJUSTMENT'") val type: String = "ADJUSTMENT",
    val counterpartyClubId: Long? = null,
    val transferId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
