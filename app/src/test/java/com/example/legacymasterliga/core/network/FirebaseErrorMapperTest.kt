package com.example.legacymasterliga.core.network

import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FirebaseErrorMapperTest {

    @Test
    fun `Firestore permission denied is not hidden as generic server failure`() {
        val error = FirebaseFirestoreException(
            "Missing or insufficient permissions.",
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
        )

        val failure = FirebaseErrorMapper.classify(error)

        assertEquals(OnlineFailureKind.PERMISSION_DENIED, failure.kind)
        assertFalse(failure.userMessage.contains("Servidor temporariamente indisponível"))
    }

    @Test
    fun `Firestore unavailable remains distinguishable from invalid credential`() {
        val error = FirebaseFirestoreException(
            "The service is currently unavailable.",
            FirebaseFirestoreException.Code.UNAVAILABLE,
        )

        val failure = FirebaseErrorMapper.classify(error)

        assertEquals(OnlineFailureKind.FIRESTORE_UNAVAILABLE, failure.kind)
        assertFalse(failure.userMessage.contains("E-mail ou senha inválidos"))
    }

    @Test
    fun `inactive and corrupt profiles have different categories`() {
        val inactive = FirebaseErrorMapper.classify(OnlineProfileInactiveException())
        val corrupt = FirebaseErrorMapper.classify(OnlineProfileCorruptException("uid-1"))

        assertEquals(OnlineFailureKind.PROFILE_INACTIVE, inactive.kind)
        assertEquals(OnlineFailureKind.PROFILE_INVALID, corrupt.kind)
    }

    @Test
    fun `missing default Firestore database is not reported as missing profile`() {
        val original = FirebaseFirestoreException(
            "The database (default) does not exist for project legacy-master-liga",
            FirebaseFirestoreException.Code.NOT_FOUND,
        )
        val wrapped = OnlineProfileBootstrapException("transaction.read-or-create", "uid-1", original)

        val failure = FirebaseErrorMapper.classify(wrapped)

        assertEquals(OnlineFailureKind.FIRESTORE_DATABASE_MISSING, failure.kind)
        assertTrue(failure.userMessage.contains("servidor de dados"))
    }

    @Test
    fun `wrapped permission denied keeps precise category and bootstrap stage`() {
        val original = FirebaseFirestoreException(
            "Missing or insufficient permissions.",
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
        )
        val wrapped = OnlineProfileBootstrapException("server.verify", "uid-1", original)

        val failure = FirebaseErrorMapper.classify(wrapped)

        assertEquals(OnlineFailureKind.PERMISSION_DENIED, failure.kind)
    }

    @Test
    fun `incomplete profile remains different from Firestore not found`() {
        val failure = FirebaseErrorMapper.classify(
            OnlineProfileIncompleteException("uid-1", listOf("username")),
        )

        assertEquals(OnlineFailureKind.PROFILE_INVALID, failure.kind)
        assertTrue(failure.userMessage.contains("incompleto"))
    }
}
