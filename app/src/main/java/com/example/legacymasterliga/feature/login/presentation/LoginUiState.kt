package com.example.legacymasterliga.feature.login.presentation

enum class LoginMode {
    LOCAL,
    ONLINE
}

enum class OnlineSubMode {
    SIGN_IN,
    SIGN_UP,
    COMPLETE_PROFILE
}

enum class OnlineAccountState {
    SIGNED_OUT,
    PROFILE_PENDING,
}

data class LoginUiState(
    val loginMode: LoginMode = LoginMode.LOCAL,
    val onlineSubMode: OnlineSubMode = OnlineSubMode.SIGN_IN,
    val username: String = "admin",
    val email: String = "",
    val password: String = "",
    val signUpName: String = "",
    val signUpNickname: String = "",
    val signUpConfirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val onlineAccountState: OnlineAccountState = OnlineAccountState.SIGNED_OUT,
    val showLocalFallback: Boolean = false,
)

internal fun LoginUiState.withFirebaseAccount(authenticated: Boolean): LoginUiState = copy(
    onlineAccountState = if (authenticated) OnlineAccountState.PROFILE_PENDING else OnlineAccountState.SIGNED_OUT,
    onlineSubMode = if (authenticated) OnlineSubMode.COMPLETE_PROFILE else onlineSubMode
)

internal fun LoginUiState.afterOnlineLogout(): LoginUiState = copy(
    password = "",
    isLoading = false,
    errorMessage = null,
    onlineAccountState = OnlineAccountState.SIGNED_OUT,
    onlineSubMode = OnlineSubMode.SIGN_IN
)
