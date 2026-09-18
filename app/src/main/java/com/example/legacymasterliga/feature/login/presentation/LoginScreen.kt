package com.example.legacymasterliga.feature.login.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.legacymasterliga.R

@Composable
fun LoginScreen(
    state: LoginUiState,
    onLoginModeChanged: (LoginMode) -> Unit,
    onOnlineSubModeChanged: (OnlineSubMode) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignUpNameChanged: (String) -> Unit,
    onSignUpNicknameChanged: (String) -> Unit,
    onSignUpConfirmPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLogin: () -> Unit,
    onLogoutOnlineAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val neonCyan = Color(0xFF00FFCC)
    val neonRed = Color(0xFFFF003F)

    Surface(modifier = modifier.fillMaxSize(), color = Color.Black) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo Estilo Retro (Vetor + Texto formatado)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_lml),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
                
                Text(
                    text = "LEGACY",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = neonCyan,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "master liga",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light,
                    color = neonCyan.copy(alpha = 0.9f),
                    modifier = Modifier.offset(y = (-8).dp)
                )
            }
            
            Text(
                text = "RETRO CHAMPIONSHIP EDITION",
                color = neonCyan.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(40.dp))

            // Seletor de Modo (LOCAL / ONLINE)
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp)
                    .border(1.dp, neonCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(4.dp),
            ) {
                LoginModeButton(
                    text = "LOCAL",
                    selected = state.loginMode == LoginMode.LOCAL,
                    neonColor = neonCyan,
                    onClick = { onLoginModeChanged(LoginMode.LOCAL) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                LoginModeButton(
                    text = "ONLINE",
                    selected = state.loginMode == LoginMode.ONLINE,
                    neonColor = neonCyan,
                    onClick = { onLoginModeChanged(LoginMode.ONLINE) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().border(
                    BorderStroke(2.dp, Brush.verticalGradient(listOf(neonCyan, Color.Transparent))),
                    RoundedCornerShape(16.dp),
                ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF001226)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val isSignUp = state.loginMode == LoginMode.ONLINE && state.onlineSubMode == OnlineSubMode.SIGN_UP
                    val isCompleteProfile = state.loginMode == LoginMode.ONLINE && state.onlineSubMode == OnlineSubMode.COMPLETE_PROFILE

                    // Cabeçalho Dinâmico
                    Text(
                        text = when {
                            state.loginMode == LoginMode.LOCAL -> "ACESSO LOCAL"
                            isSignUp -> "CRIAR NOVA CONTA"
                            isCompleteProfile -> "CONCLUIR PERFIL"
                            else -> "ENTRAR ONLINE"
                        },
                        color = neonCyan,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    if (state.loginMode == LoginMode.LOCAL) {
                        NeonTextField(
                            value = state.username,
                            onValueChange = onUsernameChanged,
                            label = stringResource(R.string.login_username),
                            icon = Icons.Outlined.Person,
                            enabled = !state.isLoading,
                            neonColor = neonCyan,
                        )
                    } else if (isCompleteProfile) {
                        Text(
                            text = "CONTA AUTENTICADA.\nINFORME SEUS DADOS PARA CONCLUIR O PERFIL.",
                            color = neonCyan,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        NeonTextField(
                            value = state.signUpName,
                            onValueChange = onSignUpNameChanged,
                            label = "SEU NOME COMPLETO",
                            icon = Icons.Outlined.Person,
                            enabled = !state.isLoading,
                            neonColor = neonCyan,
                        )
                        NeonTextField(
                            value = state.signUpNickname,
                            onValueChange = onSignUpNicknameChanged,
                            label = "SEU APELIDO / USERNAME",
                            icon = Icons.Outlined.Person,
                            enabled = !state.isLoading,
                            neonColor = neonCyan,
                        )
                    } else {
                        // Modo Online (Entrar ou Cadastrar)
                        if (isSignUp) {
                            NeonTextField(
                                value = state.signUpName,
                                onValueChange = onSignUpNameChanged,
                                label = "NOME COMPLETO",
                                icon = Icons.Outlined.Person,
                                enabled = !state.isLoading,
                                neonColor = neonCyan,
                            )
                            NeonTextField(
                                value = state.signUpNickname,
                                onValueChange = onSignUpNicknameChanged,
                                label = "APELIDO / USERNAME",
                                icon = Icons.Outlined.Person,
                                enabled = !state.isLoading,
                                neonColor = neonCyan,
                            )
                        }
                        
                        NeonTextField(
                            value = state.email,
                            onValueChange = onEmailChanged,
                            label = "E-MAIL",
                            icon = Icons.Outlined.Person,
                            enabled = !state.isLoading,
                            neonColor = neonCyan,
                            keyboardType = KeyboardType.Email,
                        )
                    }

                    if (state.loginMode == LoginMode.LOCAL || (state.loginMode == LoginMode.ONLINE && !isCompleteProfile)) {
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = onPasswordChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("SENHA", color = neonCyan.copy(alpha = 0.6f)) },
                            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = neonCyan) },
                            trailingIcon = {
                                IconButton(onClick = onTogglePasswordVisibility) {
                                    Icon(
                                        imageVector = if (state.isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = null,
                                        tint = neonCyan,
                                    )
                                }
                            },
                            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            enabled = !state.isLoading,
                            isError = state.errorMessage != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = neonCyan,
                                unfocusedTextColor = neonCyan,
                                focusedBorderColor = neonCyan,
                                unfocusedBorderColor = neonCyan.copy(alpha = 0.5f),
                                cursorColor = neonCyan,
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { if (!isSignUp) onLogin() }),
                        )
                        
                        if (isSignUp) {
                            OutlinedTextField(
                                value = state.signUpConfirmPassword,
                                onValueChange = onSignUpConfirmPasswordChanged,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("CONFIRMAR SENHA", color = neonCyan.copy(alpha = 0.6f)) },
                                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = neonCyan) },
                                visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                enabled = !state.isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = neonCyan,
                                    unfocusedTextColor = neonCyan,
                                    focusedBorderColor = neonCyan,
                                    unfocusedBorderColor = neonCyan.copy(alpha = 0.5f),
                                    cursorColor = neonCyan,
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { onLogin() }),
                            )
                        }
                    }

                    state.errorMessage?.let { message ->
                        Text(message, color = neonRed, style = MaterialTheme.typography.labelSmall)
                    }

                    if (state.showLocalFallback) {
                        Button(
                            onClick = { onLoginModeChanged(LoginMode.LOCAL) },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                            shape = RoundedCornerShape(22.dp),
                        ) {
                            Text("ENTRAR NO MODO LOCAL (OFFLINE)", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                            .border(1.dp, neonCyan, RoundedCornerShape(25.dp)),
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = neonCyan.copy(alpha = 0.1f),
                            contentColor = neonCyan,
                            disabledContainerColor = Color.DarkGray,
                        ),
                        shape = RoundedCornerShape(25.dp),
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp), color = neonCyan)
                        } else {
                            Text(
                                when {
                                    state.loginMode == LoginMode.LOCAL -> "ENTRAR AGORA"
                                    isSignUp -> "CRIAR MINHA CONTA"
                                    isCompleteProfile -> "CONCLUIR PERFIL"
                                    else -> "ENTRAR"
                                }.uppercase(),
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }

                    if (state.loginMode == LoginMode.ONLINE) {
                        if (isCompleteProfile) {
                            OutlinedButton(
                                onClick = onLogoutOnlineAccount,
                                enabled = !state.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, neonCyan.copy(alpha = 0.7f)),
                            ) {
                                Text("SAIR DESTA CONTA", color = neonCyan)
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    onOnlineSubModeChanged(if (isSignUp) OnlineSubMode.SIGN_IN else OnlineSubMode.SIGN_UP)
                                },
                                enabled = !state.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    if (isSignUp) "JÁ POSSUO CONTA. VOLTAR PARA ENTRAR" else "NÃO POSSUI CONTA? CADASTRAR AGORA", 
                                    color = neonCyan, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = if (state.loginMode == LoginMode.ONLINE) {
                            "NO MODO ONLINE, USE SEU E-MAIL.\nCONEXÃO SEGURA COM O SERVIDOR LML."
                        } else {
                            "USE O APELIDO E A SENHA PADRÃO '123456'.\nACESSO EXCLUSIVO DESTE APARELHO."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = neonCyan.copy(alpha = 0.5f),
                        lineHeight = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginModeButton(
    text: String,
    selected: Boolean,
    neonColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) neonColor.copy(alpha = 0.2f) else Color.Transparent,
            contentColor = if (selected) neonColor else neonColor.copy(alpha = 0.4f),
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(text, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    neonColor: Color,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = neonColor.copy(alpha = 0.6f)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = neonColor) },
        singleLine = true,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = neonColor,
            unfocusedTextColor = neonColor,
            focusedBorderColor = neonColor,
            unfocusedBorderColor = neonColor.copy(alpha = 0.5f),
            cursorColor = neonColor,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = keyboardType),
    )
}
