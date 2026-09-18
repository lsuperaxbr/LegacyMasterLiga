package com.example.legacymasterliga.core.network

import android.content.Context
import android.util.Log
import com.example.legacymasterliga.core.database.DatabaseContract
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConnectionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {

    fun checkConnection() {
        val initialized = FirebaseApp.getApps(context).isNotEmpty()
        val authenticated = auth.currentUser != null
        Log.i(TAG, "Firebase inicializado=$initialized; autenticado=$authenticated; banco=v${DatabaseContract.VERSION}")

        if (!initialized) {
            Log.e(TAG, "Firebase não inicializado.")
            return
        }
        if (!authenticated) {
            Log.i(TAG, "Firestore não consultado: usuário não autenticado.")
            return
        }

        // Read-only probe. The existing rules allow authenticated reads and deny every write.
        firestore.collection("system_status").document("connection_test")
            .get(Source.SERVER)
            .addOnSuccessListener {
                Log.i(TAG, "Firestore acessível (verificação somente leitura).")
            }
            .addOnFailureListener { error ->
                val code = (error as? FirebaseFirestoreException)?.code
                when (code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                        Log.w(TAG, "Firestore respondeu, mas o acesso foi negado.", error)
                    FirebaseFirestoreException.Code.UNAVAILABLE ->
                        Log.w(TAG, "Firestore indisponível ou sem conexão com a internet.", error)
                    else -> Log.w(TAG, "Falha inesperada ao consultar o Firestore.", error)
                }
            }
    }

    companion object {
        private const val TAG = "FirebaseStatus"
    }
}
