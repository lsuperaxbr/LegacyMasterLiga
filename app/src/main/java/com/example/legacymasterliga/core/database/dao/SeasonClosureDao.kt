package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.legacymasterliga.core.database.entity.FinalStandingEntity
import com.example.legacymasterliga.core.database.entity.SeasonClosureEntity

@Dao
interface SeasonClosureDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertClosure(closure: SeasonClosureEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFinalStandings(rows: List<FinalStandingEntity>)

    @Query("SELECT COUNT(*) FROM season_closures WHERE seasonId = :seasonId")
    suspend fun countBySeason(seasonId: Long): Int

}
