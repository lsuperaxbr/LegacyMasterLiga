package com.example.legacymasterliga.feature.login.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LoginUiStateTest {

    @Test
    fun `authenticated Firebase account without profile enters recoverable state`() {
        val state = LoginUiState().withFirebaseAccount(authenticated = true)

        assertEquals(OnlineAccountState.PROFILE_PENDING, state.onlineAccountState)
    }

    @Test
    fun `logout restores signed out state used by create account button`() {
        val state = LoginUiState(
            onlineAccountState = OnlineAccountState.PROFILE_PENDING,
            password = "secret",
            isLoading = true,
            errorMessage = "erro",
        ).afterOnlineLogout()

        assertEquals(OnlineAccountState.SIGNED_OUT, state.onlineAccountState)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }
}
