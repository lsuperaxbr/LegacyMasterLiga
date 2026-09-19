package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.ClubPresidencyEntity

@Dao
interface ClubPresidencyDao {
    @Insert
    suspend fun insert(entity: ClubPresidencyEntity): Long

    @Update
    suspend fun update(entity: ClubPresidencyEntity)

    @Query("SELECT * FROM club_presidencies WHERE clubId = :clubId AND endAt IS NULL LIMIT 1")
    suspend fun findOpenByClub(clubId: Long): ClubPresidencyEntity?

    @Query("""
        SELECT * FROM club_presidencies 
        WHERE clubId = :clubId AND startAt <= :atTime AND (endAt IS NULL OR endAt >= :atTime)
        LIMIT 1
    """)
    suspend fun findByClubAtTime(clubId: Long, atTime: Long): ClubPresidencyEntity?
}
