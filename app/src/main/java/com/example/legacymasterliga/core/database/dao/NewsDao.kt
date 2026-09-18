package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.legacymasterliga.core.database.entity.NewsEntity
import com.example.legacymasterliga.core.database.model.NewsFeedRow
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(news: NewsEntity): Long

    @Query("DELETE FROM news WHERE dedupKey = :dedupKey")
    suspend fun deleteByDedupKey(dedupKey: String): Int

    @Query("SELECT * FROM news WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): NewsEntity?

    @Query("""
        SELECT n.id, n.leagueId, l.name AS leagueName, n.competitionId,
               c.name AS competitionName, n.seasonId, s.name AS seasonName,
               n.title, n.body, n.category, n.eventType, n.sourceId,
               n.dedupKey, n.imageUri, n.authorUserId, n.publishedAt
        FROM news n
        JOIN leagues l ON l.id = n.leagueId
        LEFT JOIN competitions c ON c.id = n.competitionId
        LEFT JOIN seasons s ON s.id = n.seasonId
        WHERE n.leagueId = :leagueId
        ORDER BY n.publishedAt DESC, n.id DESC
    """)
    fun observeByLeague(leagueId: Long): Flow<List<NewsFeedRow>>

    @Query("SELECT COUNT(*) FROM news")
    fun observeCount(): Flow<Int>
}
