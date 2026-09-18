package com.example.legacymasterliga.feature.online.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.legacymasterliga.core.database.AppDatabase
import com.example.legacymasterliga.core.database.entity.ClubEntity
import com.example.legacymasterliga.core.database.entity.LeagueEntity
import com.example.legacymasterliga.core.database.entity.PlayerEntity
import com.example.legacymasterliga.core.model.MarketStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Utilitário Supremo de Reconstrução Soberana (v2.2).
 * Realiza o RESET NUCLEAR LOCAL e a MEGA RECONSTRUÇÃO DOS 8 ELENCOS.
 * 
 * ATENÇÃO: Para resetar a nuvem, você DEVE apagar a coleção 'leagues' no Console do Firebase.
 */
object PlayerSeeder {

    private const val OFFICIAL_LEAGUE_NAME = "LEGACY MASTER LIGA"

    suspend fun nuclearResetAndRebuild(
        context: Context, 
        dbRoom: AppDatabase,
        bootstrap: com.example.legacymasterliga.domain.usecase.InitializeDefaultDataUseCase
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d("PlayerSeeder", "!!! INICIANDO RESET NUCLEAR LOCAL !!!")

            // 1. RESET NUCLEAR LOCAL (Zera tudo no celular)
            dbRoom.clearAllTables()
            Log.d("PlayerSeeder", "Banco de dados local formatado.")

            // 2. RECRIAR CONTA ADMIN E LIGA BASE
            bootstrap()
            Log.d("PlayerSeeder", "Conta 'admin' recriada com senha 'admin123'.")

            // 3. DESVINCULAR PERFIL (Na nuvem)
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            if (auth.currentUser != null) {
                val uid = auth.currentUser!!.uid
                try {
                    firestore.collection("user_profiles").document(uid).update(
                        mapOf(
                            "cloudLeagueId" to null,
                            "memberships" to FieldValue.delete()
                        )
                    ).await()
                    Log.d("PlayerSeeder", "Vínculos de nuvem removidos do perfil.")
                } catch (e: Exception) {
                    Log.w("PlayerSeeder", "Não foi possível limpar o perfil remoto.")
                }
            }

            // 4. LOCALIZAR A LIGA RECÉM CRIADA E AJUSTAR NOME
            // O bootstrap cria a liga com nome padrão. Vamos encontrar e usar o ID dela.
            val leagueLocalId = dbRoom.query("SELECT id FROM leagues LIMIT 1", null).use { 
                if (it.moveToFirst()) it.getLong(0) else 0L 
            }
            
            // Renomeia a liga para o nome soberano
            dbRoom.query("UPDATE leagues SET name = ? WHERE id = ?", arrayOf<Any>(OFFICIAL_LEAGUE_NAME, leagueLocalId)).moveToFirst()
            Log.d("PlayerSeeder", "Liga Oficial '$OFFICIAL_LEAGUE_NAME' preparada.")

            // 5. MEGA CARGA DE 8 ELENCOS
            val megaData = mapOf(
                "St Pauli" to listOf(
                    "Vasilj", "Wahl", "Eric Smith", "Saliakas", "Oppie", "Sands", "Irvine", 
                    "Rasmussen", "Hara", "Lage", "Hountondji", "Voll", "Mets", "Nemeth", 
                    "Ritzka", "Pyrka", "Fujita", "Metcalfe", "Nick Schmidt", "Kaars", 
                    "Ricky-Jade", "Ceesay", "Stevens", "Robatsch", "Ando", "Dzwigala", 
                    "Spari", "Schmitz", "Sinani", "Gazdov"
                ),
                "Red Bull Salzburg" to listOf(
                    "Alex Schlager", "Chase", "Gadou", "Lainer", "Kratzig", "Bidstrup", 
                    "Kjærgaard", "Kitano", "Alajbegovic", "Vertessen", "Karim Konate", 
                    "Zawieschitzky", "Tim Drexler", "Mellberg", "Terzic", "Trummer", 
                    "Souma Diabate", "Kawamura", "Oliver Lukic", "Yeo", "Redzac", "Onisiwo", 
                    "Enrique Aguilar", "Bischoff", "Diambou", "Sulzbacher", "Baidoo", 
                    "Omoregie", "Schuster"
                ),
                "LDU" to listOf(
                    "Valle", "Richard Mina", "Allala", "Ade", "Jose Quintero", "Leo Quinonez", 
                    "Pretell", "Alex Alvarado", "Redes", "Janner Corozo", "Deyverson", 
                    "A. Dominguez", "Luis Segovia", "Minda", "Tobar", "Dani De La Cruz", 
                    "Cuero", "Ed Castillo", "Cornejo", "Yerlin Quinonez", "Paul Duran", 
                    "Jeison Medina", "Michael Estrada", "Alexis Villa", "Villamil"
                ),
                "Levante" to listOf(
                    "M. Ryan", "Matias Moreno", "Elgezabal", "Dela", "Toljan", "Oriol Rey", 
                    "Vencedor", "Carlos Alvarez", "Pablo Martinez", "Ivan Romero", "Brugue", 
                    "Pablo Campos", "Arriaga", "Matturro", "Pampin", "Manu Sanchez", 
                    "Olasagasti", "Losada", "Jose Morales", "Victor Garcia", "Koyalipou", 
                    "Espi", "Jorge Cabello", "Eyong", "Krug"
                ),
                "Vasco" to listOf(
                    "Daniel Fuzato", "Cuesta", "Lucas Freitas", "Puma Rodriguez", "Lucas Piton", 
                    "Jair", "Matheus Franca", "Hinestroza", "GB", "David", "Brenner", 
                    "Leo Jardim", "Robert Renan", "Alan Saldivia", "Cuiabano", "Paulo Henrique", 
                    "Thiago Mendes", "Estrella", "Adson", "Nuno Moreira", "Andres Gomez", 
                    "Spinelli", "Cauan Barros", "Hugo Moura", "Tche Tche", "Mateus Carvalho", 
                    "Johan Rojas", "Caua Paixao", "Victor Luis"
                ),
                "Athletico Paranaense" to listOf(
                    "Mycael", "Lucas Belezi", "Leo Pele", "Luiz Gustavo", "Raul", "Bruno Zapelli", 
                    "G. Benavidez", "Lucas Esquivel", "Joao Cruz", "Leozinho", "Kevin Viveros", 
                    "Santos", "Carlos Teran", "Felipe Aguirre", "Jadson", "Gilberto", "Leo Derik", 
                    "Ale Garcia", "Isaac", "Daniel Aguilar", "Mendoza", "Luiz Fernando", 
                    "Julimar", "Juan Portilla", "Renan Viana", "Felipinho", "Renan Peixoto"
                ),
                "Pisa" to listOf(
                    "Semper", "Caracciolo", "Calabresi", "Albiol", "Hojholt", "Aebischer", 
                    "Juan Cuadrado", "Angori", "Stengs", "Tramoni", "Nzola", "Scuffet", 
                    "Canestrelli", "Fran Coppola", "Denoon", "Maucci", "Akinsanmiro", 
                    "Idrissa Toure", "Leris", "Moreo", "Meister", "Lorran", "Louis Buffon", 
                    "Marius Marin", "Mbambi", "Bonfanti", "Vural", "Tomas Esteves", 
                    "Mateus Lusuardi", "Piccinini", "Vukovic"
                ),
                "Chapecoense" to listOf(
                    "Anderson", "Rafael Thyere", "Joao Paulo", "Marcos Vinicius", "Walter Clar", 
                    "Bruno Pacheco", "Giovanni A.", "Carvalheira", "Marcio Junior", 
                    "Jean Carlos", "Bolasie", "Bruno Leonardo", "Victor Caetano", "Joao Vitor", 
                    "Gustavo Talles", "Higor Meritao", "Robert", "Camilo", "Marcinho", 
                    "Joao Bom", "Rubens", "Italo Vargas", "Enio", "Neto Pessoa", 
                    "Mauricio Garcez", "Doma"
                )
            )

            var totalAtletas = 0
            megaData.forEach { (clubName, players) ->
                val clubId = dbRoom.clubDao().insert(
                    ClubEntity(
                        leagueId = leagueLocalId,
                        name = clubName,
                        isActive = true
                    )
                )

                players.forEach { name ->
                    dbRoom.playerDao().insert(
                        PlayerEntity(
                            leagueId = leagueLocalId,
                            clubId = clubId,
                            name = name,
                            marketStatus = MarketStatus.NOT_LISTED,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    totalAtletas++
                }
            }

            logAndToast(context, "RECONSTRUÇÃO LOCAL CONCLUÍDA: $totalAtletas atletas carregados!")

        } catch (e: Exception) {
            Log.e("PlayerSeeder", "FALHA NO RESET: ${e.message}", e)
            logAndToast(context, "Erro no Reset: ${e.message}")
        }
    }

    private suspend fun logAndToast(context: Context, msg: String) = withContext(Dispatchers.Main) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}
