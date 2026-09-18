package com.example.legacymasterliga.data.repository

import com.example.legacymasterliga.core.database.dao.LeagueDao
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import com.example.legacymasterliga.data.mapper.toDomain
import com.example.legacymasterliga.domain.model.League
import com.example.legacymasterliga.domain.repository.LeagueRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomLeagueRepository @Inject constructor(
    private val leagueDao: LeagueDao,
) : LeagueRepository {
    override fun observeAll(): Flow<List<League>> =
        leagueDao.observeAll().map { leagues -> leagues.map { it.toDomain() } }

    override suspend fun findByName(name: String): League? =
        leagueDao.findByName(name.trim())?.toDomain()

    override suspend fun create(name: String, currencyCode: String): Long {
        val cleanName = name.trim()
        val cleanCurrency = currencyCode.trim().uppercase()
        require(cleanName.length >= 3) { "O nome da liga deve ter pelo menos 3 caracteres." }
        require(cleanCurrency == "CR") { "A moeda oficial do Legacy Master Liga é CR." }
        check(leagueDao.findByName(cleanName) == null) { "Já existe uma liga com esse nome." }
        return leagueDao.insert(
            LeagueEntity(
                name = cleanName,
                currencyCode = cleanCurrency,
            ),
        )
    }
}
