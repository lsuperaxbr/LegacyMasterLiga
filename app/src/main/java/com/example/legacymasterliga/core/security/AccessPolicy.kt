package com.example.legacymasterliga.core.security

import com.example.legacymasterliga.core.model.UserRole

object AccessPolicy {
    fun canManageUsers(role: UserRole): Boolean = role == UserRole.ADMINISTRATOR || role == UserRole.PRESIDENT
    fun canManageLeague(role: UserRole): Boolean = role == UserRole.ADMINISTRATOR || role == UserRole.PRESIDENT
    fun canManageClubs(role: UserRole): Boolean = role == UserRole.ADMINISTRATOR || role == UserRole.PRESIDENT
    fun canManageResults(role: UserRole): Boolean = role == UserRole.ADMINISTRATOR || role == UserRole.PRESIDENT
    fun canManageFinance(role: UserRole): Boolean = role == UserRole.ADMINISTRATOR
    fun canViewFinance(role: UserRole): Boolean = role != UserRole.VISITOR
    fun canViewMarket(role: UserRole): Boolean = role != UserRole.VISITOR
    fun canViewPublicData(role: UserRole): Boolean = true
}
