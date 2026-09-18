package com.example.legacymasterliga.domain.repository

import com.example.legacymasterliga.domain.model.League
import kotlinx.coroutines.flow.Flow

interface LeagueRepository {
    fun observeAll(): Flow<List<League>>
    suspend fun findByName(name: String): League?
    suspend fun create(name: String, currencyCode: String): Long
}
