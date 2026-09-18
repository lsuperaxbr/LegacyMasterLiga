package com.example.legacymasterliga.core.security

import com.example.legacymasterliga.core.model.UserRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessPolicyTest {
    @Test fun administrator_has_full_management_access() {
        val role = UserRole.ADMINISTRATOR
        assertTrue(AccessPolicy.canManageUsers(role))
        assertTrue(AccessPolicy.canManageLeague(role))
        assertTrue(AccessPolicy.canManageClubs(role))
        assertTrue(AccessPolicy.canManageResults(role))
        assertTrue(AccessPolicy.canManageFinance(role))
        assertTrue(AccessPolicy.canViewFinance(role))
        assertTrue(AccessPolicy.canViewMarket(role))
    }

    @Test fun president_can_view_private_club_data_but_cannot_manage() {
        val role = UserRole.PRESIDENT
        assertFalse(AccessPolicy.canManageUsers(role))
        assertFalse(AccessPolicy.canManageLeague(role))
        assertFalse(AccessPolicy.canManageResults(role))
        assertTrue(AccessPolicy.canViewFinance(role))
        assertTrue(AccessPolicy.canViewMarket(role))
        assertTrue(AccessPolicy.canViewPublicData(role))
    }

    @Test fun visitor_has_public_read_only_access() {
        val role = UserRole.VISITOR
        assertFalse(AccessPolicy.canManageClubs(role))
        assertFalse(AccessPolicy.canViewFinance(role))
        assertFalse(AccessPolicy.canViewMarket(role))
        assertTrue(AccessPolicy.canViewPublicData(role))
    }
}
