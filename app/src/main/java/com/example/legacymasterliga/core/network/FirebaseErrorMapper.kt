package com.example.legacymasterliga.core.network

import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.TimeoutCancellationException

enum class OnlineFailureKind {
    INVALID_CREDENTIAL,
    AUTH_UNAVAILABLE,
    NO_INTERNET,
    PERMISSION_DENIED,
    PROFILE_NOT_FOUND,
    PROFILE_INACTIVE,
    PROFILE_INVALID,
    WRONG_FIREBASE_PROJECT,
    TIMEOUT,
    FIRESTORE_UNAVAILABLE,
    FIRESTORE_DATABASE_MISSING,
    UNEXPECTED,
}

data class OnlineFailure(
    val kind: OnlineFailureKind,
    val userMessage: String,
    val technicalMessage: String?,
)

class FirebaseProjectMismatchException(actual: String?) : IllegalStateException(
    "Projeto Firebase incorreto. Esperado legacy-master-liga; recebido ${actual ?: "sem projectId"}.",
)

class OnlineProfileNotFoundException : IllegalStateException("Perfil online inexistente.")

class OnlineProfileInactiveException : IllegalStateException("O perfil autenticado não está ativo.")

class OnlineProfileCorruptException(uid: String, detail: String? = null) : IllegalStateException(
    buildString {
        append("Perfil online $uid inválido")
        if (!detail.isNullOrBlank()) append(": $detail")
    },
)

class OnlineProfileIncompleteException(uid: String, missingFields: Collection<String>) : IllegalStateException(
    "Perfil online $uid incompleto. Faltam: ${missingFields.sorted().joinToString()}.",
)

class OnlineProfileBootstrapException(
    stage: String,
    uid: String,
    cause: Throwable,
) : IllegalStateException(
    "Falha na configuração do perfil ($stage).",
    cause,
)

class OnlineProfileIdentityMismatchException(expectedUid: String, actualUid: String?) : IllegalStateException(
    "Identidade divergente. Esperado $expectedUid; recebido ${actualUid ?: "nenhum usuário"}.",
)

object FirebaseErrorMapper {
    fun map(error: Throwable): String = classify(error).userMessage

    fun classify(error: Throwable): OnlineFailure {
        val chain = causeChain(error)
        val cause = chain.firstOrNull { it is TimeoutCancellationException }
            ?: chain.firstOrNull { it is FirebaseProjectMismatchException }
            ?: chain.firstOrNull { it is OnlineProfileNotFoundException }
            ?: chain.firstOrNull { it is OnlineProfileInactiveException }
            ?: chain.firstOrNull { it is OnlineProfileCorruptException }
            ?: chain.firstOrNull { it is OnlineProfileIncompleteException }
            ?: chain.firstOrNull { it is OnlineProfileIdentityMismatchException }
            ?: chain.firstOrNull { it is OnlineProfileBootstrapException }
            ?: chain.firstOrNull { it is FirebaseNetworkException }
            ?: chain.firstOrNull { it is FirebaseApiNotAvailableException }
            ?: chain.firstOrNull { it is FirebaseAuthException }
            ?: chain.firstOrNull { it is FirebaseFirestoreException }
            ?: chain.firstOrNull { it is IllegalStateException }
            ?: chain.last()
            
        return when (cause) {
            is TimeoutCancellationException -> failure(OnlineFailureKind.TIMEOUT, "A conexão com o servidor demorou muito. Verifique sua internet e tente novamente.", cause)
            is FirebaseProjectMismatchException -> failure(OnlineFailureKind.WRONG_FIREBASE_PROJECT, "Erro de configuração do servidor. Contate o suporte.", cause)
            is OnlineProfileNotFoundException -> failure(OnlineFailureKind.PROFILE_NOT_FOUND, "Perfil não localizado. Conclua seu cadastro para continuar.", cause)
            is OnlineProfileInactiveException -> failure(OnlineFailureKind.PROFILE_INACTIVE, "Esta conta está desativada ou bloqueada.", cause)
            is OnlineProfileCorruptException -> failure(OnlineFailureKind.PROFILE_INVALID, "Dados de perfil inconsistentes. Contate o administrador.", cause)
            is OnlineProfileIncompleteException -> failure(OnlineFailureKind.PROFILE_INVALID, "Seu perfil está incompleto. Por favor, conclua o cadastro dos dados obrigatórios.", cause)
            is OnlineProfileIdentityMismatchException -> failure(OnlineFailureKind.PROFILE_INVALID, "A conta conectada não corresponde aos dados locais. Saia e entre novamente.", cause)
            is OnlineProfileBootstrapException -> classifyBootstrap(cause)
            is FirebaseNetworkException -> failure(OnlineFailureKind.NO_INTERNET, "Sem conexão com a internet. Verifique sua rede.", cause)
            is FirebaseApiNotAvailableException -> failure(OnlineFailureKind.AUTH_UNAVAILABLE, "O serviço de autenticação não está disponível neste dispositivo.", cause)
            is FirebaseAuthInvalidCredentialsException -> failure(OnlineFailureKind.INVALID_CREDENTIAL, "E-mail ou senha incorretos.", cause)
            is FirebaseAuthInvalidUserException -> failure(OnlineFailureKind.INVALID_CREDENTIAL, "Usuário não encontrado. Verifique o e-mail ou crie uma nova conta.", cause)
            is FirebaseAuthUserCollisionException -> failure(OnlineFailureKind.INVALID_CREDENTIAL, "Este e-mail já possui uma conta. Entre com sua senha.", cause)
            is FirebaseAuthException -> when (cause.errorCode) {
                "ERROR_OPERATION_NOT_ALLOWED" -> failure(OnlineFailureKind.AUTH_UNAVAILABLE, "O acesso por e-mail e senha está temporariamente desativado.", cause)
                "ERROR_WEAK_PASSWORD" -> failure(OnlineFailureKind.INVALID_CREDENTIAL, "A senha informada é muito curta ou fraca. Tente uma senha mais forte.", cause)
                "ERROR_INVALID_EMAIL" -> failure(OnlineFailureKind.INVALID_CREDENTIAL, "O formato do e-mail é inválido.", cause)
                "ERROR_USER_DISABLED" -> failure(OnlineFailureKind.PROFILE_INACTIVE, "Esta conta foi desativada.", cause)
                "ERROR_TOO_MANY_REQUESTS" -> failure(OnlineFailureKind.AUTH_UNAVAILABLE, "Muitas tentativas falhas. Tente novamente em alguns minutos.", cause)
                "ERROR_NETWORK_REQUEST_FAILED" -> failure(OnlineFailureKind.NO_INTERNET, "Falha de comunicação com o servidor. Verifique sua internet.", cause)
                "ERROR_EMAIL_ALREADY_IN_USE", "account-exists-with-different-credential" -> failure(OnlineFailureKind.INVALID_CREDENTIAL, "Este e-mail já possui uma conta. Entre com sua senha.", cause)
                else -> failure(OnlineFailureKind.AUTH_UNAVAILABLE, "Não foi possível realizar a autenticação. Tente novamente mais tarde.", cause)
            }
            is FirebaseFirestoreException -> classifyFirestore(cause)
            is IllegalStateException -> failure(OnlineFailureKind.UNEXPECTED, cause.message ?: "Falha na operação online.", cause)
            else -> {
                val userFriendlyMsg = when {
                    cause is java.net.UnknownHostException || cause is java.net.ConnectException -> "Sem conexão com a internet."
                    else -> "Não foi possível concluir a operação online. Tente novamente."
                }
                failure(OnlineFailureKind.UNEXPECTED, userFriendlyMsg, cause)
            }
        }
    }

    private fun classifyBootstrap(error: OnlineProfileBootstrapException): OnlineFailure {
        val firestoreError = causeChain(error).filterIsInstance<FirebaseFirestoreException>().firstOrNull()
            ?: return failure(OnlineFailureKind.UNEXPECTED, "Não foi possível finalizar a configuração do seu perfil online.", error)
        return classifyFirestore(firestoreError, error.message)
    }

    private fun classifyFirestore(
        error: FirebaseFirestoreException,
        technicalMessage: String? = error.message,
    ): OnlineFailure = when (error.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> failure(
            OnlineFailureKind.PERMISSION_DENIED,
            "Acesso negado. Você não tem permissão para acessar estes dados no servidor.",
            error,
            technicalMessage,
        )
        FirebaseFirestoreException.Code.UNAVAILABLE -> failure(
            OnlineFailureKind.FIRESTORE_UNAVAILABLE,
            "O servidor de dados está temporariamente indisponível. Verifique sua conexão.",
            error,
            technicalMessage,
        )
        FirebaseFirestoreException.Code.NOT_FOUND -> {
            val databaseMissing = error.message.orEmpty().contains("database (default) does not exist", ignoreCase = true)
            failure(
                if (databaseMissing) OnlineFailureKind.FIRESTORE_DATABASE_MISSING else OnlineFailureKind.PROFILE_NOT_FOUND,
                if (databaseMissing) {
                    "O servidor de dados ainda não foi configurado."
                } else {
                    "As informações solicitadas não foram encontradas no servidor de dados."
                },
                error,
                technicalMessage,
            )
        }
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> failure(
            OnlineFailureKind.TIMEOUT,
            "A consulta ao servidor demorou muito para responder. Tente novamente.",
            error,
            technicalMessage,
        )
        else -> failure(
            OnlineFailureKind.UNEXPECTED,
            "Ocorreu uma falha na comunicação com o servidor de dados.",
            error,
            "${error.code.name}: ${technicalMessage ?: "Erro desconhecido"}"
        )
    }

    private fun causeChain(error: Throwable): List<Throwable> {
        val result = mutableListOf<Throwable>()
        var current: Throwable? = error
        while (current != null && current !in result) {
            result += current
            current = current.cause
        }
        return result
    }

    private fun failure(
        kind: OnlineFailureKind,
        message: String,
        error: Throwable,
        technicalMessage: String? = error.message,
    ) = OnlineFailure(kind, message, technicalMessage)
}
