package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.legacymasterliga.core.database.entity.ArenaDuelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArenaDao {
    @Insert
    suspend fun insert(duel: ArenaDuelEntity): Long

    @Query("UPDATE arena_duels SET status = :status, resultType = :resultType, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun resolve(id: Long, status: String, resultType: String, resolvedAt: Long)

    @Query("SELECT * FROM arena_duels WHERE id = :id")
    suspend fun findById(id: Long): ArenaDuelEntity?

    @Query("""
        SELECT d.*, a.name AS clubAName, b.name AS clubBName
        FROM arena_duels d
        JOIN clubs a ON a.id = d.clubAId
        JOIN clubs b ON b.id = d.clubBId
        WHERE d.leagueId = :leagueId
        ORDER BY d.createdAt DESC
    """)
    fun observeByLeague(leagueId: Long): Flow<List<ArenaDuelRow>>
}

data class ArenaDuelRow(
    val id: Long, val leagueId: Long, val clubAId: Long, val clubAName: String,
    val clubBId: Long, val clubBName: String, val stakeCr: Long, val status: String,
    val resultType: String?, val note: String?, val createdByUserId: Long?,
    val createdAt: Long, val resolvedAt: Long?,
)
