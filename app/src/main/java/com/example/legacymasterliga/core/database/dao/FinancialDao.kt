package com.example.legacymasterliga.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.legacymasterliga.core.database.entity.FinancialTransactionEntity
import com.example.legacymasterliga.core.database.model.FinancialStatementRow
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: FinancialTransactionEntity): Long

    @Update
    suspend fun update(transaction: FinancialTransactionEntity)

    @Query("SELECT * FROM financial_transactions WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): FinancialTransactionEntity?

    @Query("SELECT COALESCE(SUM(amountCr), 0) FROM financial_transactions WHERE clubId = :clubId")
    suspend fun balanceOf(clubId: Long): Long

    @Query("SELECT COALESCE(SUM(amountCr), 0) FROM financial_transactions WHERE clubId = :clubId")
    fun observeBalance(clubId: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM financial_transactions WHERE clubId = :clubId AND type = :type")
    suspend fun countByClubAndType(clubId: Long, type: String): Int

    @Query("""
        SELECT ft.id, ft.clubId, c.name AS clubName, ft.amountCr, ft.description,
               ft.type, ft.counterpartyClubId, cp.name AS counterpartyName,
               ft.transferId, ft.createdAt
        FROM financial_transactions ft
        JOIN clubs c ON c.id = ft.clubId
        LEFT JOIN clubs cp ON cp.id = ft.counterpartyClubId
        WHERE c.leagueId = :leagueId AND (:clubId IS NULL OR ft.clubId = :clubId)
        ORDER BY ft.createdAt DESC, ft.id DESC
    """)
    fun observeStatement(leagueId: Long, clubId: Long?): Flow<List<FinancialStatementRow>>

    @Query("""
        SELECT c.id, c.name, c.isBank, COALESCE(SUM(ft.amountCr), 0) AS balanceCr
        FROM clubs c LEFT JOIN financial_transactions ft ON ft.clubId = c.id
        WHERE c.leagueId = :leagueId AND (c.isActive = 1 OR c.isBank = 1)
        GROUP BY c.id ORDER BY c.isBank DESC, c.name COLLATE NOCASE
    """)
    fun observeClubBalances(leagueId: Long): Flow<List<com.example.legacymasterliga.core.database.model.ClubBalanceRow>>

    @Query("""
        SELECT COALESCE(SUM(ft.amountCr), 0) 
        FROM financial_transactions ft 
        JOIN clubs c ON c.id = ft.clubId 
        WHERE c.leagueId = :leagueId AND c.isBank = 0
    """)
    fun observeTotalBalance(leagueId: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM financial_transactions")
    fun observeTransactionCount(): Flow<Int>
}
